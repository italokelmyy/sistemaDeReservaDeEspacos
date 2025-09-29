# Projeto Full-Stack com Flutter e Spring: Ideias para Iniciantes

Aqui estão algumas ideias de projetos que fogem do clichê (lista de tarefas, blog, e-commerce) e que são ótimas para aprender e construir um portfólio sólido como desenvolvedor full-stack júnior.

O foco é em aplicações com regras de negócio simples, mas que cobrem os fundamentos essenciais: CRUD (Create, Read, Update, Delete), autenticação, e relacionamentos entre entidades.

---

### 1. Gerenciador de Plantas Domésticas

Um aplicativo para ajudar usuários a cuidarem de suas plantas, com lembretes e um diário de bordo para cada uma.

-   **Descrição:** O usuário cadastra suas plantas, define a frequência de rega e adubação, e o app envia notificações como lembrete. Ele também pode manter um histórico de cuidados, como "podado em...", "trocado de vaso em...".
-   **Features do Backend (Spring Boot):**
    -   API REST com endpoints para `Usuarios` e `Plantas`.
    -   Autenticação de usuário com JWT (Login/Registro).
    -   CRUD completo para as plantas de um usuário.
    -   Um endpoint para registrar um "evento" no histórico da planta (ex: `POST /plantas/{id}/historico`).
    -   **(Extra) Um `@Scheduled` job que roda uma vez por dia para verificar quais plantas precisam de cuidados e poderia (no futuro) disparar e-mails ou push notifications.**
-   **Features do Frontend (Flutter):**
    -   Telas de Login e Registro.
    -   Uma tela principal com a lista de plantas do usuário.
    -   Indicadores visuais para plantas que precisam de atenção (ex: um ícone de gota d'água).
    -   Tela de detalhes da planta, mostrando suas informações e o histórico de cuidados.
    -   Formulário para adicionar/editar uma planta.
-   **Por que é um bom projeto?**
    -   Ensina relacionamentos (um usuário tem muitas plantas).
    -   Permite trabalhar com datas e agendamento.
    -   É um projeto com apelo visual e funcional.

---

### 2. Biblioteca de Ferramentas do Bairro

Um sistema para que vizinhos de um condomínio ou bairro possam emprestar e pegar emprestado ferramentas uns dos outros.

-   **Descrição:** Um usuário pode listar as ferramentas que tem disponíveis para emprestar (furadeira, escada, etc.). Outro usuário pode ver a lista de ferramentas disponíveis e solicitar um empréstimo.
-   **Features do Backend (Spring Boot):**
    -   API REST para `Usuarios`, `Ferramentas` e `Emprestimos`.
    -   Autenticação de usuário com JWT.
    -   CRUD para as ferramentas (cada ferramenta pertence a um usuário).
    -   Lógica de negócio para o empréstimo:
        -   Mudar o status da ferramenta para "Emprestada".
        -   Registrar quem pegou emprestado e a data.
        -   Endpoint para "devolver" a ferramenta, mudando seu status de volta para "Disponível".
-   **Features do Frontend (Flutter):**
    -   Telas de Login/Registro.
    -   Tela com a lista de todas as ferramentas disponíveis na comunidade.
    -   Tela "Minhas Ferramentas", onde o usuário gerencia o que ele cadastrou.
    -   Tela de detalhes da ferramenta com um botão para "Solicitar Empréstimo".
    -   Uma tela "Meus Empréstimos" para ver o que o usuário pegou emprestado.
-   **Por que é um bom projeto?**
    -   Trabalha com o conceito de "estado" de um objeto (Disponível, Emprestada).
    -   Ótimo para praticar lógica de negócio e controle de acesso simples.
    -   Simula um problema real de compartilhamento de recursos.

---

### 3. Rastreador de Assinaturas e Contas

Um app para ajudar o usuário a controlar seus gastos recorrentes, como Netflix, Spotify, contas de água, luz, etc.

-   **Descrição:** O usuário cadastra suas assinaturas e contas, informando o valor e a data de vencimento/cobrança. O app exibe um dashboard com o total de gastos mensais e alerta sobre contas que estão para vencer.
-   **Features do Backend (Spring Boot):**
    -   API REST para `Usuarios` e `Assinaturas`.
    -   Autenticação com JWT.
    -   CRUD para as assinaturas (com campos como `nome`, `valor`, `ciclo` (mensal/anual), `data_vencimento`).
    -   Um endpoint de "dashboard" que retorna um resumo: gasto total do mês, próximas 5 contas a vencer, etc.
    -   Lógica para, ao pagar uma conta, avançar a data de vencimento para o próximo ciclo.
-   **Features do Frontend (Flutter):**
    -   Telas de Login/Registro.
    -   Dashboard principal com gráficos simples (ex: um anel mostrando o total gasto).
    -   Lista de todas as assinaturas, talvez separadas por categoria (Streaming, Contas de Casa, etc.).
    -   Formulário para adicionar/editar uma assinatura.
    -   Notificações locais (usando um pacote Flutter) para alertar sobre vencimentos próximos.
-   **Por que é um bom projeto?**
    -   Envolve manipulação de datas e cálculos financeiros simples.
    -   Permite criar uma UI com dashboards e visualização de dados, que é um diferencial.
    -   É uma ferramenta genuinamente útil no dia a dia.
