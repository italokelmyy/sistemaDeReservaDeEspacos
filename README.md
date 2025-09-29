# Sistema de Reserva de Espaço

API RESTful para um sistema completo de gerenciamento de reservas de espaços físicos (como salas de reunião, auditórios, etc.). O sistema foi projetado para evitar conflitos de horários, gerenciar usuários e salas, e fornecer uma base sólida para futuras expansões.

O projeto é totalmente containerizado com Docker, facilitando a configuração e execução do ambiente de desenvolvimento.

---

## ✨ Principais Funcionalidades

- **Gerenciamento de Usuários:** Cadastro e autenticação de usuários com JWT (JSON Web Tokens).
- **Gerenciamento de Salas:** Adição, remoção e listagem de salas disponíveis.
- **Sistema de Reservas:** Criação, cancelamento e listagem de reservas de salas.
- **Comunicação Assíncrona:** Uso do RabbitMQ para processamento de eventos (ex: notificações de reserva).
- **Segurança:** Acesso aos endpoints protegido por autenticação baseada em token.
- **Ambiente Containerizado:** Configuração simplificada com Docker e Docker Compose.

---

## 🛠️ Tecnologias Utilizadas

- **Backend:**
  - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
  - [Spring Boot 3](https://spring.io/projects/spring-boot)
  - [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
  - [Spring Security](https://spring.io/projects/spring-security)
- **Banco de Dados:**
  - [MySQL 8.0](https://www.mysql.com/)
- **Mensageria:**
  - [RabbitMQ](https://www.rabbitmq.com/)
- **Autenticação:**
  - [JWT (JSON Web Tokens)](https://jwt.io/)
- **Build & Dependências:**
  - [Apache Maven](https://maven.apache.org/)
- **Containerização:**
  - [Docker](https://www.docker.com/)
  - [Docker Compose](https://docs.docker.com/compose/)

---

## 🚀 Como Executar o Projeto

Para executar este projeto, você precisará ter **Docker** e **Docker Compose** instalados em sua máquina.

### 1. Clone o Repositório

```bash
git clone https://github.com/italokelmyy/sistemaDeReservaDeEspacos.git
cd sistemaDeReservaDeEspaco
```

### 2. Crie o Arquivo de Ambiente (`.env`)

Na raiz do projeto, crie um arquivo chamado `.env`. Copie e cole o conteúdo abaixo, substituindo os valores entre `<...>` pelas suas próprias configurações.

```env
SPRING_PROFILES_ACTIVE=docker
# JWT
JWT_SECRET_KEY=<coloque_aqui_sua_chave_secreta_jwt>
JWT_EXPIRATION=600000

# SSL
SERVER_SSL_KEY_STORE_PASSWORD=<sua_senha_do_keystore>


# Banco de dados (Docker)
SPRING_DATASOURCE_USERNAME=<seu_usuario_bd>
SPRING_DATASOURCE_PASSWORD=<sua_senha_bd>

# RabbitMQ
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# MYSQL Container

MYSQL_DATABASE=sistemareservasdeespacos
MYSQL_USER=<seu_usuario_bd>
MYSQL_PASSWORD=<sua_senha_bd>
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/sistemareservasdeespacos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
MYSQL_ROOT_PASSWORD=<sua_senha_root_do_mysql>
```
**Atenção:** O arquivo `keystore.p12` deve estar presente na pasta `src/main/resources`.


### 3. Suba os Contêineres com Docker Compose

Com o arquivo `.env` configurado, execute o seguinte comando no terminal, a partir da raiz do projeto:

```bash
docker-compose up --build
```

Este comando irá:
- Fazer o build da imagem Docker da sua aplicação Spring Boot.
- Baixar e iniciar os contêineres do `MySQL` e `RabbitMQ`.
- Iniciar a sua aplicação.
- Criar uma rede interna para que os contêineres se comuniquem.

### 4. Acesse a Aplicação

Após a inicialização completa, os serviços estarão disponíveis nos seguintes endereços:

- **API do Sistema de Reservas:** `https://localhost:8443`
- **Painel de Gerenciamento do RabbitMQ:** `http://localhost:15672` (usuário: `guest`, senha: `guest`)

---

## 📋 Endpoints da API

Aqui estão alguns dos principais endpoints disponíveis:

### Autenticação
- `POST /usuario/cadastro`: Cria um novo usuário.
- `POST /usuario/login`: Autentica um usuário e retorna um token JWT.

### Salas
- `GET /sala/lista`: Lista todas as salas.
- `POST /sala/adicionarSala`: Adiciona uma nova sala (requer autenticação).
- `GET /sala/findById/{id}`: Busca uma sala pelo ID (requer autenticação).
- `GET /sala/removerSala/{id}`: Remove uma sala (requer autenticação).

### Reservas
- `POST /reserva/adicionar`: Cria uma nova reserva para uma sala (requer autenticação).
- `GET /reserva/lista`: Lista todas as salas reservadas.
- `GET /reserva/deleteById/{id}`: Cancela/deleta uma reserva (requer autenticação).
```