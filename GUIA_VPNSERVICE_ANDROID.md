
# Desvendando o `VpnService` do Android: Um Guia Completo

Este guia é um mergulho profundo e focado exclusivamente na classe `android.net.VpnService`. O objetivo é entender, do zero, como construir a fundação de qualquer aplicativo de VPN no Android, do ponto de vista nativo (Java/Kotlin).

---

### O Que é e Por Que Existe?

O `VpnService` é uma classe base fornecida pelo Android que permite que um aplicativo crie uma interface de rede virtual e atue como um gateway para o tráfego de rede do dispositivo. 

**Por que ele é necessário?** Por segurança. Interceptar e modificar o tráfego de rede de um usuário é uma operação extremamente poderosa e perigosa. O Android força os desenvolvedores a usar o `VpnService` para garantir que:

1.  **O Usuário Esteja no Controle:** O aplicativo **deve** obter permissão explícita do usuário através de um diálogo padrão do sistema antes de iniciar a VPN.
2.  **Haja Transparência:** Quando uma VPN está ativa, o Android exibe um ícone persistente (uma chave) na barra de status, informando ao usuário que seu tráfego está sendo roteado.
3.  **O Processo Seja Padronizado:** Ele fornece um framework seguro e estável para a criação e gerenciamento da interface de rede.

---

## Parte 1: O Ciclo de Vida (A Jornada para se Tornar uma VPN)

O processo para ativar seu `VpnService` segue uma ordem rigorosa.

#### Passo 1: Pedir a Permissão (`VpnService.prepare`)

Antes de qualquer coisa, você precisa da "licença". Isso é feito com o método estático `VpnService.prepare()`.

-   **Como funciona?** Você chama `VpnService.prepare(context)`. 
    -   Se o seu app **ainda não tem permissão**, este método retorna um `Intent`. Você deve lançar este `Intent` com `startActivityForResult()`. Isso fará com que o Android mostre a caixa de diálogo padrão: "[Seu App] quer configurar uma conexão VPN...".
    -   Se o seu app **já tem permissão** (o usuário aceitou em uma sessão anterior), o método retorna `null`.

#### Passo 2: Iniciar o Serviço (`startService`)

Depois que o usuário concede a permissão (você recebe o resultado em `onActivityResult` com `RESULT_OK`), seu aplicativo está autorizado a iniciar o serviço. 

Você faz isso como faria com qualquer outro serviço do Android: criando um `Intent` que aponta para a sua classe que herda de `VpnService` e chamando `context.startService(intent)`.

#### Passo 3: A Execução (`onStartCommand`)

Quando o serviço é iniciado, o método `onStartCommand()` da sua classe `VpnService` é chamado. É aqui que a mágica começa. Dentro deste método, você irá:

1.  Configurar a interface de rede virtual (usando o `Builder`).
2.  Estabelecer a conexão.
3.  Iniciar um loop para ler e escrever pacotes de rede.

#### Passo 4: Parar o Serviço

Para parar a VPN, você pode chamar `stopService()` de outras partes do seu app, ou o próprio serviço pode chamar `stopSelf()` para se autodestruir. Isso acionará o processo de limpeza.

#### Passo 5: A Limpeza (`onDestroy`)

Quando o serviço está sendo destruído, o método `onDestroy()` é chamado. É sua responsabilidade fechar todos os recursos abertos, como os `streams` da interface e interromper quaisquer `threads` em execução para evitar vazamentos de memória e consumo de bateria.

---

## Parte 2: Construindo a Interface (As Ferramentas do `Builder`)

Dentro de `onStartCommand`, a primeira tarefa é configurar e criar a interface de rede virtual. Isso é feito com a classe aninhada `VpnService.Builder`.

```java
// Dentro de onStartCommand...
VpnService.Builder builder = new VpnService.Builder();
```

Estes são os métodos mais importantes do `Builder`:

-   **`builder.setSession(String name)`**
    -   **O que faz?** Define o nome da sua sessão de VPN. Esse nome é exibido para o usuário nas configurações de VPN do Android.
    -   **Exemplo:** `builder.setSession("MinhaVPNApp");`

-   **`builder.addAddress(String address, int prefixLength)`**
    -   **O que faz?** Define o endereço IP **virtual** da sua interface de rede. É o endereço que o próprio celular terá *dentro* da sua rede privada.
    -   **Exemplo:** `builder.addAddress("10.0.0.2", 24);`
    -   **Explicação:** Isso configura a interface com o IP `10.0.0.2` e uma máscara de sub-rede de `255.255.255.0` (indicada pelo prefixo `/24`).

-   **`builder.addRoute(String address, int prefixLength)`**
    -   **O que faz?** Este é o método **mais importante**. Ele diz ao Android qual tráfego deve ser redirecionado para a sua VPN.
    -   **Exemplo:** `builder.addRoute("0.0.0.0", 0);`
    -   **Explicação:** A rota `0.0.0.0/0` é uma rota padrão que significa **"absolutamente todo o tráfego IPv4"**. Ao adicionar esta rota, você está dizendo ao sistema: "Envie tudo para mim". Se você quisesse capturar tráfego apenas para um servidor específico, poderia usar algo como `builder.addRoute("8.8.8.8", 32);`.

-   **`builder.addDnsServer(String address)`**
    -   **O que faz?** Configura o servidor DNS que será usado enquanto a VPN estiver ativa. Essencial para que a resolução de nomes (ex: `google.com` -> `172.217.169.142`) funcione.
    -   **Exemplo:** `builder.addDnsServer("8.8.8.8");`

-   **`builder.establish()`**
    -   **O que faz?** Finaliza a configuração e estabelece a interface de rede virtual. 
    -   **Retorno:** Ele retorna um objeto `ParcelFileDescriptor`. Este objeto não é um arquivo no disco, mas sim um "descritor" ou um "ponteiro" para a interface de rede que acabou de ser criada. É a sua porta de entrada e saída para o tráfego de rede do celular.

---

## Parte 3: Manipulando o Tráfego (Lendo e Escrevendo Pacotes)

Com o `ParcelFileDescriptor` em mãos, você pode começar a "inspecionar a carga".

1.  **Obter os Streams:** Você obtém um `FileInputStream` e um `FileOutputStream` a partir do `FileDescriptor` do `ParcelFileDescriptor`.

    ```java
    ParcelFileDescriptor vpnInterface = builder.establish();
    FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
    FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
    ```

2.  **O Fluxo de Dados:**
    -   **`FileInputStream` (Leitura):** Tudo o que você **lê** deste `stream` são os pacotes IP brutos que os aplicativos no celular estão tentando **enviar para a internet**. 
    -   **`FileOutputStream` (Escrita):** Tudo o que você **escreve** neste `stream` são pacotes IP brutos que você quer **entregar para os aplicativos no celular**, como se tivessem vindo da internet.

3.  **O Loop Principal:**
    O coração de um serviço de VPN é um loop infinito (geralmente em uma `Thread` separada para não travar o app) que constantemente lê pacotes do `FileInputStream`, processa-os e, se necessário, escreve as respostas no `FileOutputStream`.

    ```java
    // Buffer para armazenar os dados do pacote
    ByteBuffer packet = ByteBuffer.allocate(32767);

    while (true) {
        // Lê um pacote da rede
        int length = in.read(packet.array());
        if (length > 0) {
            // Pacote interceptado! 
            // AQUI VOCÊ FAZ SUA LÓGICA:
            // 1. Analisar o cabeçalho do pacote (origem, destino, protocolo).
            // 2. Criptografar o pacote.
            // 3. Enviar para seu servidor VPN remoto.
            // 4. Esperar a resposta do servidor.
            // 5. Descriptografar a resposta.
            // 6. Escrever a resposta no `FileOutputStream`.
        }
        packet.clear();
    }
    ```

---

### Resumo Final

Ser um `VpnService` é um processo de 3 etapas principais:

1.  **Burocracia (`prepare`, `startService`):** Obter a licença do usuário e iniciar o serviço.
2.  **Construção (`Builder`):** Configurar sua "agência de alfândega", definindo seu endereço virtual e quais "cargas" (rotas) você quer inspecionar.
3.  **Operação (`Streams` e `Loop`):** Estabelecer o posto de inspeção e começar o trabalho de ler, processar e escrever os pacotes de dados que formam toda a comunicação do celular com a internet.

Embora seja complexo, o `VpnService` é uma API extremamente lógica e poderosa. Dominá-la é o único caminho para criar qualquer tipo de aplicativo que manipule o tráfego de rede no Android.
