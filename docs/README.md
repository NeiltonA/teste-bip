# 🏗️ Desafio Fullstack Integrado
🚨 Instrução Importante (LEIA ANTES DE COMEÇAR)
❌ NÃO faça fork deste repositório.

Este repositório é fornecido como modelo/base. Para realizar o desafio, você deve:
✅ Opção correta (obrigatória)
  Clique em “Use this template” (se este repositório estiver marcado como Template)
OU
  Clone este repositório e crie um NOVO repositório público em sua conta GitHub.
📌 O resultado deve ser um repositório próprio, independente deste.

## 🎯 Objetivo
Criar solução completa em camadas (DB, EJB, Backend, Frontend), corrigindo bug em EJB e entregando aplicação funcional.

## 📦 Estrutura
- db/: scripts schema e seed
- ejb-module/: serviço EJB com bug a ser corrigido
- bip-beneficios-backend/: backend Java 8+
- bip-beneficios-frontend/: app Angular
- docs/: instruções e critérios
- .github/workflows/: CI

## ✅ Tarefas do candidato
1. Executar db/schema.sql e db/seed.sql
2. Corrigir bug no BeneficioEjbService
3. Implementar backend CRUD + integração com EJB
4. Desenvolver frontend Angular consumindo backend
5. Implementar testes
6. Documentar (Swagger, README)
7. Enviar link para recrutadora com seu repositório para análise

## 🐞 Bug no EJB
- Transferência não verifica saldo, não usa locking, pode gerar inconsistência
- Espera-se correção com validações, rollback, locking/optimistic locking

## 📊 Critérios de avaliação
- Arquitetura em camadas (20%)
- Correção EJB (20%)
- CRUD + Transferência (15%)
- Qualidade de código (10%)
- Testes (15%)
- Documentação (10%)
- Frontend (10%)

## Implementacao entregue

- `ejb-module`: entidade `Beneficio`, servico de transferencia com locking pessimista e excecoes de negocio com rollback.
- `bip-beneficios-backend`: API REST Spring Boot com CRUD, transferencia, validacao, tratamento de erros, Swagger e organizacao por modulo (`controller`, `service`, `repository`, `dto`, `model`).
- `bip-beneficios-frontend`: app Angular com tela de gestao e formulario de transferencia organizados por feature em `src/app/modules/beneficios`.
- `db`: scripts SQL de schema e seed.
- `.github/workflows`: CI separado para Java e Angular.

## Validacoes locais

```powershell
cd d:\bip-beneficios
mvn test

cd d:\bip-beneficios\bip-beneficios-frontend
npm test
npm run build
```
