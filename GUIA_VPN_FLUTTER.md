# Guia: Criando um VPN Local em Flutter para Filtragem de Domínios

Este guia explica como criar um serviço de VPN local e nativo para um aplicativo Flutter. O objetivo é inspecionar o tráfego de rede para identificar nomes de domínio (via DNS e SNI) e bloquear conexões se o domínio contiver palavras-chave predefinidas.

## 1. O que é um "VPN Local"?

Diferente de um VPN tradicional que redireciona seu tráfego para um servidor remoto, um **VPN Local** (ou *on-device VPN*) cria uma interface de rede virtual no próprio dispositivo. Todo o tráfego de saída do dispositivo é roteado para essa interface, permitindo que seu aplicativo o intercepte, inspecione, modifique ou bloqueie antes que ele chegue à rede real.

**Vantagens:**
- **Privacidade:** O tráfego não sai do dispositivo para um terceiro.
- **Performance:** A latência é mínima, pois não há um servidor remoto.
- **Controle Total:** Seu aplicativo se torna um firewall programável.

## 2. Arquitetura da Solução com Flutter

A lógica do VPN é puramente nativa (Kotlin/Java para Android, Swift/Obj-C para iOS). O Flutter atua como a interface de usuário (UI) e o ponto de controle.

A arquitetura se divide em 3 partes:

1.  **Interface Flutter (Dart):**
    *   Botões para iniciar e parar o serviço de VPN.
    *   Uma área para o usuário gerenciar a lista de palavras-chave a serem bloqueadas.
    *   Exibe o status atual do serviço (ativo, inativo, erro).

2.  **Canal de Comunicação (MethodChannel):**
    *   É a ponte que permite que o código Dart (Flutter) se comunique com o código nativo (Android/iOS).
    *   Comandos que o Flutter enviará: `startVpn`, `stopVpn`, `updateKeywords`.
    *   Informações que o nativo pode enviar de volta: `statusChanged`, `domainBlocked`.

3.  **Serviço de VPN Nativo (Kotlin/Java ou Swift):**
    *   O coração da funcionalidade.
    *   Intercepta todos os pacotes de rede (IP).
    *   Analisa os pacotes para extrair o domínio de destino.
    *   Decide se bloqueia ou permite a conexão.

![Arquitetura VPN Local com Flutter](https://i.imgur.com/9A7f4iB.png)

## 3. Como Inspecionar Nomes de Domínio

Existem duas técnicas principais para descobrir o domínio ao qual o usuário está tentando se conectar:

### a) Inspeção de DNS (Domínio: `*`)

-   **O que é:** O DNS (Domain Name System) é o sistema que traduz nomes de domínio (como `google.com`) para endereços IP. As consultas DNS geralmente acontecem via protocolo UDP na porta 53.
-   **Como funciona:**
    1.  O VPN intercepta um pacote UDP destinado à porta 53.
    2.  Seu código analisa a estrutura do pacote DNS para extrair o nome de domínio que está sendo consultado.
    3.  Você compara o domínio com sua lista de palavras-chave.
    4.  Se houver uma correspondência, você simplesmente **descarta o pacote**. O dispositivo nunca receberá o endereço IP e a conexão falhará.

-   **Limitação:** **DNS over HTTPS (DoH)** e **DNS over TLS (DoT)** criptografam as consultas DNS, tornando essa técnica ineficaz.

### b) Inspeção de SNI (Domínio: `HTTPS`)

-   **O que é:** SNI (Server Name Indication) é uma extensão do protocolo TLS (usado pelo HTTPS). Durante o handshake inicial de uma conexão segura, o cliente envia o nome do domínio ao qual deseja se conectar em **texto claro**.
-   **Como funciona:**
    1.  O VPN intercepta um pacote TCP destinado à porta 443 (porta padrão do HTTPS).
    2.  Seu código analisa o pacote para identificar se é uma mensagem `Client Hello` do TLS.
    3.  Dentro do `Client Hello`, você extrai a extensão SNI, que contém o nome do domínio.
    4.  Compara o domínio com sua lista de palavras-chave.
    5.  Se houver correspondência, você fecha a conexão ou descarta os pacotes relacionados a ela.

-   **Vantagem:** Funciona mesmo se o DNS estiver criptografado.
-   **Limitação Futura:** Uma nova tecnologia chamada **Encrypted Client Hello (ECH)**, anteriormente conhecida como ESNI, está sendo desenvolvida para criptografar também o SNI, o que tornará essa técnica obsoleta no futuro.

## 4. Implementação (Foco no Android)

A implementação no Android é feita com a classe `VpnService`.

### Passo 1: Configurar o Lado do Flutter

Crie um serviço em Dart para gerenciar a comunicação.

```dart
// lib/vpn_service.dart
import 'package:flutter/services.dart';

class VpnService {
  static const MethodChannel _channel = MethodChannel('com.example.myapp/vpn');

  static Future<void> startVpn(List<String> blockedKeywords) async {
    try {
      await _channel.invokeMethod('start', {'keywords': blockedKeywords});
    } on PlatformException catch (e) {
      print("Falha ao iniciar o VPN: '${e.message}'.");
    }
  }

  static Future<void> stopVpn() async {
    try {
      await _channel.invokeMethod('stop');
    } on PlatformException catch (e) {
      print("Falha ao parar o VPN: '${e.message}'.");
    }
  }
}
```

### Passo 2: Configurar o Lado Nativo (Android)

#### a) Permissões no `AndroidManifest.xml`

Adicione a permissão para vincular o serviço de VPN.

```xml
<!-- file: android/app/src/main/AndroidManifest.xml -->
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
    <application ...>
        ...
        <service
            android:name=".MyVpnService"
            android:permission="android.permission.BIND_VPN_SERVICE"
            android:exported="false">
            <intent-filter>
                <action android:name="android.net.VpnService" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

#### b) Criar a Classe `MyVpnService.kt`

Esta é a classe principal que herda de `VpnService`.

```kotlin
// file: android/app/src/main/kotlin/com/example/myapp/MyVpnService.kt
package com.example.myapp

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class MyVpnService : VpnService() {
    private var vpnThread: Thread? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    
    companion object {
        var blockedKeywords: List<String> = emptyList()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Inicia a thread que vai lidar com o tráfego
        vpnThread = Thread {
            try {
                // 1. Configura a interface VPN
                vpnInterface = configureVpn()

                // 2. Roda o loop de leitura/escrita de pacotes
                runVpnLoop(vpnInterface!!)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopSelf()
            }
        }
        vpnThread?.start()
        return START_STICKY
    }

    private fun configureVpn(): ParcelFileDescriptor {
        val builder = Builder()
        builder.setMtu(1500)
        builder.addAddress("10.0.0.2", 24) // Endereço IP virtual da interface VPN
        builder.addRoute("0.0.0.0", 0)     // Roteia TODO o tráfego para a VPN
        builder.addDnsServer("8.8.8.8")      // Servidor DNS (pode ser qualquer um)
        builder.setSession("MyVpnService")
        return builder.establish() ?: throw IllegalStateException("Falha ao estabelecer VPN")
    }

    private fun runVpnLoop(vpnInterface: ParcelFileDescriptor) {
        val vpnInput = FileInputStream(vpnInterface.fileDescriptor)
        val vpnOutput = FileOutputStream(vpnInterface.fileDescriptor)
        val packet = ByteBuffer.allocate(32767)

        while (true) {
            val bytesRead = vpnInput.read(packet.array())
            if (bytesRead > 0) {
                packet.limit(bytesRead)
                
                // TODO: Analisar o pacote aqui (DNS ou SNI)
                val domain = parseDomainFromPacket(packet) // Função a ser implementada
                
                val shouldBlock = blockedKeywords.any { keyword -> domain?.contains(keyword, ignoreCase = true) == true }

                if (shouldBlock) {
                    // Se bloquear, simplesmente não encaminhe o pacote
                    println("BLOQUEADO: $domain")
                } else {
                    // Se permitir, escreva o pacote de volta para a rede real
                    vpnOutput.write(packet.array(), 0, bytesRead)
                }
                packet.clear()
            }
        }
    }
    
    // Função de exemplo (e simplificada) para análise de pacotes
    private fun parseDomainFromPacket(packet: ByteBuffer): String? {
        // Esta é a parte mais complexa.
        // Você precisará de uma biblioteca ou de um código robusto para
        // analisar o cabeçalho IP, depois o cabeçalho TCP/UDP, e finalmente
        // o payload (DNS ou TLS/SNI).
        //
        // Exemplo de pseudocódigo:
        // 1. Ler cabeçalho IP para pegar o protocolo (TCP/UDP) e portas.
        // 2. Se for UDP na porta 53 -> Chamar um parser de DNS.
        // 3. Se for TCP na porta 443 -> Chamar um parser de TLS Client Hello para SNI.
        return "example.com" // Retorno de exemplo
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnThread?.interrupt()
        vpnInterface?.close()
    }
}
```

#### c) Ligar o Flutter ao Código Nativo

Em `MainActivity.kt`, configure o `MethodChannel`.

```kotlin
// file: android/app/src/main/kotlin/com/example/myapp/MainActivity.kt
package com.example.myapp

import android.content.Intent
import android.net.VpnService
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.myapp/vpn"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "start" -> {
                    val keywords = call.argument<List<String>>("keywords") ?: emptyList()
                    MyVpnService.blockedKeywords = keywords
                    
                    val intent = VpnService.prepare(this)
                    if (intent != null) {
                        startActivityForResult(intent, 0) // Pede permissão ao usuário
                    } else {
                        onActivityResult(0, RESULT_OK, null) // Permissão já concedida
                    }
                    result.success(null)
                }
                "stop" -> {
                    val intent = Intent(this, MyVpnService::class.java)
                    stopService(intent)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val intent = Intent(this, MyVpnService::class.java)
            startService(intent)
        }
    }
}
```

## 5. Desafios e Próximos Passos

-   **Análise de Pacotes:** A parte mais difícil é a função `parseDomainFromPacket`. Analisar pacotes de rede binários é complexo e propenso a erros. Recomenda-se usar bibliotecas de terceiros para isso, como [NetBare](https://github.com/httptoolkit/netbare) para Kotlin.
-   **Consumo de Bateria:** Processar cada pacote de rede do dispositivo consome bateria. O código precisa ser o mais eficiente possível.
-   **Implementação para iOS:** No iOS, o processo é conceitualmente similar, mas utiliza o `Network Extension Framework` da Apple e a classe `NEPacketTunnelProvider`. A configuração é mais restrita e exige uma conta de desenvolvedor da Apple.
-   **Estabilidade:** Um bug no seu serviço de VPN pode fazer com que o dispositivo perca toda a conectividade com a internet. Teste exaustivamente.

Este guia fornece a base para você começar. A jornada é desafiadora, mas o resultado é um controle poderoso sobre o tráfego de rede do seu aplicativo.
