# BibliotecaRestClient
Sistema de biblioteca feito com dois micro serviços em Quarkus, que se comunicam por REST Client.

- **catalog** (porta 9080): cuida dos livros.
- **loans** (porta 9081): cuida dos usuários, do login com JWT e dos empréstimos. Para emprestar e devolver, chama o catalog.

## Endpoints

catalog:

- POST /books: cadastra um livro
- GET /books: lista os livros (pode filtrar com `?title=`)
- GET /books/{id}: busca um livro
- PUT /books/{id}/loan: marca como emprestado
- PUT /books/{id}/return: marca como devolvido

loans:

- POST /users: cadastra um usuário
- POST /auth/login: faz login e devolve o token
- GET /loans/books: lista os livros do catalog
- GET /loans: lista os empréstimos (pode filtrar com `?borrower=`)
- POST /loans: cria um empréstimo
- PUT /loans/{id}/return: devolve um empréstimo

Os endpoints de `/loans` exigem o token do login, enviado no cabeçalho `Authorization: Bearer <token>`.

## Plano de Teste

### 1. Escopo

**Será testado**

- catalog: cadastrar, listar (com filtro), buscar, emprestar e devolver livro.
- loans: cadastrar usuário, login (JWT), criar, listar e devolver empréstimos.
- Integração entre loans e catalog.

**Não será testado**

- Persistência (os dados ficam em memória) e interface gráfica (o sistema só tem API REST).
- Testes de carga e de segurança.

### 2. Estratégia

| Nível | O que valida | Ferramentas |
|---|---|---|
| Estático | Más práticas no código | PMD |
| Unitário | Cada classe isolada (catalog simulado com Mockito) | JUnit 5, Mockito, JaCoCo |
| Componente | Cada serviço pela API REST | `@QuarkusTest`, RestAssured |
| Sistema | Os dois serviços reais no ar, pela API | RestAssured, JUnit 5 |

Os testes cobrem casos de sucesso, de falha (404, 409, 401) e de borda. Não há teste de interface porque o sistema não tem interface web.

### 3. Critérios de entrada e saída

**Entrada:** o código compila e os dois serviços sobem nas portas 9080 e 9081.

**Saída:** `mvn verify` com BUILD SUCCESS em catalog e loans, PMD sem violações acima do limite e cobertura de linhas de pelo menos 80%. Os testes de sistema passam.

### 4. Riscos e mitigação

| Risco | Mitigação |
|---|---|
| O teste de sistema depende dos dois serviços no ar | Subir catalog e loans antes de rodar |
| O loans depende do catalog | No componente o catalog é simulado; no sistema usa-se o real |
| O estado fica em memória e vaza entre testes | Cada teste cria seus próprios dados |

### 5. Cronograma

| Semana | Atividade |
|---|---|
| 1 | Plano de teste, PMD e JaCoCo |
| 2 | Testes unitários |
| 3 | Testes de componente |
| 4 | Testes de sistema e ajustes finais |

### 6. Ambiente e ferramentas

Java 21, Maven, Quarkus 3.38.3, JUnit 5, Mockito, RestAssured, PMD e JaCoCo.
