# Plano de Teste: BibliotecaRestClient

Sistema de biblioteca com dois serviços: o catalog (porta 9080), que cuida dos livros, e o loans (porta 9081), que cuida dos usuários, do login e dos empréstimos.

## 1. Escopo

Vai ser testado:

- catalog: cadastrar, listar, buscar, emprestar e devolver livro.
- loans: cadastrar usuário, fazer login, criar, listar e devolver empréstimo.
- A comunicação entre o loans e o catalog.

Não vai ser testado:

- Banco de dados, porque os dados ficam só na memória.
- Tela, porque o sistema só tem API.
- Teste de carga e de segurança.

## 2. Estratégia

- Estático: o PMD procura erros comuns no código.
- Unitário: testa cada classe sozinha, com JUnit e Mockito. O JaCoCo mede a cobertura.
- Componente: testa cada serviço pela API, com RestAssured.
- Sistema: testa os dois serviços juntos, também pela API.

Os testes olham o que dá certo, o que dá erro (404, 409, 401) e casos de limite.

## 3. Critérios de entrada e saída

- Entrada: o código compila e os dois serviços sobem.
- Saída: `mvn verify` passa nos dois serviços, o PMD não reprova e a cobertura é de pelo menos 80%. Os testes de sistema passam.

## 4. Riscos e mitigação

- O teste de sistema precisa dos dois serviços ligados: ligar os dois antes de rodar.
- O loans depende do catalog: nos testes de componente o catalog é simulado.
- Os dados ficam na memória e podem sobrar de um teste para o outro: cada teste cria seus próprios dados.

## 5. Cronograma

- Semana 1: plano de teste, PMD e JaCoCo.
- Semana 2: testes unitários.
- Semana 3: testes de componente.
- Semana 4: testes de sistema e ajustes finais.

## 6. Ambiente e ferramentas

Java 21, Maven, Quarkus, JUnit 5, Mockito, RestAssured, PMD e JaCoCo.
