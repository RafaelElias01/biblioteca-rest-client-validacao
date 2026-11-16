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

Vai ser testado:

- catalog: cadastrar, listar, buscar, emprestar e devolver livro.
- loans: cadastrar usuário, fazer login, criar, listar e devolver empréstimo.
- A comunicação entre o loans e o catalog.

### 2. Estratégia

- Estático: o PMD procura erros comuns no código.
- Unitário: testa cada classe sozinha, com JUnit e Mockito. O JaCoCo mede a cobertura.
- Componente: testa cada serviço pela API, com RestAssured.
- Sistema: testa os dois serviços juntos, também pela API.

Os testes olham o que dá certo, o que dá erro (404, 409, 401) e casos de limite.

### 3. Critérios de entrada e saída

- Entrada: o código compila e os dois serviços sobem.
- Saída: `mvn verify` passa nos dois serviços, o PMD não reprova e a cobertura é de pelo menos 80%. Os testes de sistema passam.

### 4. Riscos e mitigação

- O teste de sistema precisa dos dois serviços ligados: ligar os dois antes de rodar.
- O loans depende do catalog: nos testes de componente o catalog é simulado.
- Os dados ficam na memória e podem sobrar de um teste para o outro: cada teste cria seus próprios dados.

### 5. Cronograma

- Semana 1: plano de teste, PMD e JaCoCo.
- Semana 2: testes unitários.
- Semana 3: testes de componente.
- Semana 4: testes de sistema e ajustes finais.

### 6. Ambiente e ferramentas

Java 21, Maven, Quarkus, JUnit 5, Mockito, RestAssured, PMD e JaCoCo.
