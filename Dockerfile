# ETAPA 1: A Oficina de Construção (Build Stage)
# Analogia: Pense nesta etapa como uma cozinha industrial completa. Temos todas as ferramentas
# pesadas (JDK, Maven) para pegar ingredientes crus (seu código-fonte) e transformá-los em um prato
# pronto (o arquivo .jar executável da sua aplicação).

# 1. Imagem Base do Builder (FROM)
# O que faz: Define a imagem base para esta etapa de construção. maven:3.9.8-eclipse-temurin-21 é uma imagem oficial que já vem com o Java Development Kit (JDK) versão 21 e o Maven 3.9.8 instalados.
# Por que usar: Precisamos de um ambiente com todas as ferramentas necessárias para compilar nosso código Java. Usar uma imagem específica com versões fixas (3.9.8-eclipse-temurin-21) garante que seus builds sejam consistentes e reproduzíveis, evitando problemas de "funciona na minha máquina". O AS builder dá um apelido a esta etapa, permitindo que a referenciemos mais tarde em um build multi-stage.
# Analogia: É como escolher o tipo de fundação para o seu apartamento. Você precisa de uma fundação que suporte uma estrutura de Java 21.
# Conselho: Sempre use builds multi-stage para aplicações Java. A primeira etapa (builder) contém as ferramentas de compilação, e a segunda (final) contém apenas o ambiente de execução, resultando em imagens menores e mais seguras.
FROM maven:3.9.8-eclipse-temurin-21 AS builder

# 2. Diretório de Trabalho (WORKDIR)
# O que faz: Define o diretório de trabalho padrão para todas as instruções RUN, CMD, ENTRYPOINT, COPY e ADD que se seguem.
# Por que usar: É uma boa prática definir um diretório de trabalho logo no início. Isso mantém seu projeto organizado dentro do contêiner, evitando caminhos absolutos longos e garantindo que os comandos sejam executados no contexto correto.
# Analogia: Pense nisso como escolher uma "mesa de trabalho" limpa e dedicada para o seu projeto dentro da oficina.
WORKDIR /app

# 3. Otimização de Cache para Dependências (COPY e RUN)
# O que faz: Copia apenas o arquivo pom.xml (que lista as dependências do seu projeto Maven) para o diretório de trabalho atual (/app) dentro do contêiner.
COPY pom.xml .
# O que faz: Executa o comando Maven para baixar todas as dependências do projeto e armazená-las no cache local do Maven dentro do contêiner.
# Por que usar: Esta é uma técnica crucial para otimização de cache em builds Docker. As dependências do projeto (pom.xml) geralmente mudam com muito menos frequência do que o código-fonte. Ao copiar e baixar as dependências separadamente, o Docker pode "cachear" esta camada. Se o pom.xml não mudar em um build subsequente, o Docker reutilizará a camada cacheada, economizando muito tempo ao não precisar baixar as dependências novamente.
# Analogia: Imagine um chef que, ao receber uma nova receita (pom.xml), primeiro busca todos os ingredientes na despensa (mvn dependency:go-offline). Se a receita não mudar na próxima vez, ele não precisa ir à despensa novamente, pois os ingredientes já estão à mão.
RUN mvn dependency:go-offline

# 4. Copiando e Compilando o Código (COPY e RUN)
# O que faz: Copia todo o código-fonte da sua aplicação (a pasta src) para o diretório /app/src dentro do contêiner.
COPY src ./src
# O que faz: Executa o comando Maven para compilar seu código-fonte e empacotá-lo em um arquivo .jar executável (geralmente em target/seu-app.jar). A flag -Dmaven.test.skip=true é usada para pular a execução dos testes unitários e de integração durante o build da imagem, o que acelera o processo. Os testes devem ser executados em um pipeline de CI/CD separado.
# Analogia: Com todos os ingredientes na mesa, o chef segue o modo de preparo (o código) e cozinha o prato principal (mvn package).
RUN mvn package -Dmaven.test.skip=true

# ETAPA 2: A Vitrine de Exposição (Runtime Stage)
# Analogia: Nosso prato está pronto. Agora, o tiramos da cozinha industrial (a etapa builder) e o
# colocamos em uma "vitrine" limpa, leve e elegante para ser servido. A vitrine não precisa de fornos e
# batedeiras, apenas do prato pronto e de um ambiente mínimo para exibi-lo.

# 1. Imagem Base de Execução (FROM)
# O que faz: Inicia uma nova etapa de construção com uma imagem base diferente. eclipse-temurin:21-jre é uma imagem oficial que contém apenas o Java Runtime Environment (JRE) versão 21.
# Por que usar: O JRE é muito menor que o JDK (que inclui ferramentas de desenvolvimento e compilação). Para a imagem final que será executada em produção, só precisamos do ambiente para rodar a aplicação, não para compilá-la. Isso resulta em imagens Docker muito menores, mais rápidas para baixar e mais seguras (menos componentes = menos superfícies de ataque).
# Conselho: Sempre use uma imagem base mínima para a etapa final em um build multi-stage.
FROM eclipse-temurin:21-jre

# 2. Instalação de Ferramentas Essenciais (RUN)
# O que faz: Atualiza a lista de pacotes do sistema e instala a ferramenta curl. O rm -rf /var/lib/apt/lists/* limpa o cache de pacotes para manter a imagem pequena.
# Por que usar: Imagens JRE são mínimas e não vêm com muitas ferramentas. curl é frequentemente necessário para verificações de saúde (HEALTHCHECK) ou para depuração básica. Instalamos apenas o essencial.
# Analogia: Nossa vitrine precisa de um pequeno termômetro (curl) para verificar a "saúde" do prato que está sendo servido.
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# 3. Copiando o Artefato Final (WORKDIR e COPY)
# O que faz: Define novamente o diretório de trabalho para esta etapa.
WORKDIR /app
# O que faz: Esta é a "mágica" do build multi-stage. Copiamos apenas o arquivo .jar compilado da etapa anterior (a "oficina" ou builder) para o diretório de trabalho atual (/app) na nossa nova imagem leve. Nenhum código-fonte, dependências de build ou ferramentas de compilação são incluídos na imagem final.
# Por que usar: Garante que a imagem final seja o mais enxuta possível, contendo apenas o que é estritamente necessário para executar a aplicação.
COPY --from=builder /app/target/sistemaDeReservaDeEspaco-0.0.1-SNAPSHOT.jar app.jar

# 4. Segurança: Usuário Não-Root (RUN e USER)
# O que faz: Cria um novo usuário e grupo de sistema chamado spring.
RUN adduser --system --group spring
# O que faz: Define que as instruções subsequentes e o comando ENTRYPOINT serão executados com as permissões deste novo usuário (spring) e grupo (spring).
# Por que usar: Por padrão, os processos dentro de um contêiner Docker rodam como o usuário root, o que é um grande risco de segurança. Se um invasor conseguir explorar uma vulnerabilidade na sua aplicação, ele terá privilégios de root dentro do contêiner. Rodar a aplicação como um usuário não-root com privilégios mínimos (spring) reduz drasticamente o impacto de um possível ataque.
# Conselho: SEMPRE adote esta prática em ambientes de produção. Pense nisso como não entregar a "chave mestra da loja" (root) para qualquer funcionário.
USER spring:spring

# 5. Verificação de Saúde (HEALTHCHECK)
# O que faz: Ensina o Docker como verificar se sua aplicação está realmente saudável e pronta para receber requisições.
# --interval=30s: O Docker verificará a saúde a cada 30 segundos.
# --timeout=5s: O comando de verificação deve responder em até 5 segundos.
# --start-period=30s: Dá à aplicação 30 segundos para iniciar antes de começar a falhar o health check. Útil para aplicações que demoram a inicializar.
# CMD curl -fk https://localhost:8443/actuator/health || exit 1: O comando executado. Ele tenta fazer uma requisição HTTPS para o endpoint /actuator/health da sua aplicação na porta 8443. Se a requisição falhar (por exemplo, a aplicação não está respondendo ou o endpoint não está disponível), o comando curl retornará um erro, e o || exit 1 garante que o health check falhe.
# Por que usar: O Docker (e orquestradores como Kubernetes) usam o health check para saber se um contêiner está funcionando corretamente. Se um contêiner falha no health check, ele pode ser reiniciado automaticamente, garantindo a disponibilidade da sua aplicação.
# Analogia: É como um termômetro (curl) que verifica a temperatura do prato a cada 30 segundos para garantir que ele está na temperatura ideal para ser servido.
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
  CMD curl -fk https://localhost:8443/actuator/health || exit 1

# 6. Expondo a Porta (EXPOSE)
# O que faz: Informa ao Docker que o contêiner escuta na porta 8443 em tempo de execução.
# Por que usar: É uma instrução de documentação. Ela não publica a porta para o mundo exterior (isso é feito no docker-compose.yml ou no comando docker run), mas ajuda quem lê o Dockerfile a entender qual porta a aplicação usa. Também é usada por ferramentas de orquestração para configurar redes.
# Analogia: Uma placa na vitrine dizendo "Servimos nesta porta: 8443".
EXPOSE 8443

# 7. Comando de Inicialização (ENTRYPOINT e CMD)
# ENTRYPOINT ["java", "-jar", "app.jar"]: Define o comando principal e imutável que será executado quando o contêiner iniciar. Neste caso, é o comando para executar seu arquivo .jar Java. O ENTRYPOINT é sempre executado.
# CMD ["--spring.profiles.active=dev"]: Fornece argumentos padrão para o ENTRYPOINT. Se você não especificar nenhum argumento ao iniciar o contêiner, o CMD será anexado ao ENTRYPOINT. No entanto, se você fornecer argumentos ao iniciar o contêiner (como fizemos no docker-compose.yml com environment: - SPRING_PROFILES_ACTIVE=docker), o CMD será ignorado. Por isso, o CMD original foi comentado, pois o perfil ativo é definido via variável de ambiente no docker-compose.yml.
# Analogia: ENTRYPOINT é como a instrução "Ligue o forno e asse o bolo". CMD seria o "com a cobertura de chocolate" – um detalhe padrão que pode ser mudado (por exemplo, para "com a cobertura de baunilha") ao dar a instrução.
ENTRYPOINT ["java", "-jar", "app.jar"]