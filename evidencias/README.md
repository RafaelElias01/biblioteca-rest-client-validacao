# Evidências de execução

Relatórios gerados por `mvn verify` (catalog e loans) e pelo teste de sistema (com os dois serviços no ar, portas 9080 e 9081), em 20/11/2026.

| Pasta | Conteúdo |
|---|---|
| `catalog/pmd`, `loans/pmd` | Análise estática (PMD): abra `pmd.html` |
| `catalog/jacoco`, `loans/jacoco` | Cobertura (JaCoCo): abra `index.html` |
| `catalog/testes`, `loans/testes`, `system-tests` | Resultado dos testes (surefire) |

Resultado: catalog 21 testes, loans 22 testes, sistema 8 testes, todos passando. Cobertura de linhas: 100% nos dois serviços.

O nome de usuário do Windows nos caminhos foi trocado por `USUARIO`.
