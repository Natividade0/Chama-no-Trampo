# Roadmap - Chama no Trampo

Este roadmap organiza o desenvolvimento por fases para evitar uma lista solta de features.

## Fase atual: MVP local honesto

Objetivo: validar o fluxo principal no aparelho, sem Firebase ainda.

Ja implementado:

```text
- Demandas e ofertas
- Categorias: Vaga, Bico, Servico
- Urgente como prioridade separada
- Publicacao local
- Busca e filtros
- Salvos
- Perfil local
- Perfil publico simples
- Minhas publicacoes
- Editar anuncio
- Excluir anuncio
- Renovar anuncio
- Marcar como concluido
- Expiracao local
- Ocultar e marcar como suspeito
- Bloquear anunciante por telefone/authorKey
```

## Proxima fase: preparacao para login

Objetivo: preparar UX e codigo para login real sem quebrar o modo local.

Itens:

```text
- Tela visual: Entrar com telefone
- Opcao: Continuar testando localmente
- Estado local: modoLocal ou usuarioLogado
- Bloquear futuras funcionalidades reais atras de login
```

Importante:

- Nao criar login fake enganoso.
- Nao dizer "telefone verificado" antes de Firebase Auth Phone.
- Modo local deve ficar claro como modo de teste.

## Fase Firebase Auth Phone

Objetivo: transformar identidade local em identidade real.

Itens:

```text
- Configurar Firebase
- Adicionar google-services.json
- Ajustar Gradle com cuidado
- Firebase Auth Phone
- userId real
- telefone verificado
- perfil vinculado ao userId
```

Regras de produto:

```text
- Publicar exige login
- Salvar exige login
- Bloquear exige login
- Ocultar/denunciar exige login
- Avaliar exige login e job concluido
```

## Fase Firestore

Objetivo: levar dados para nuvem com seguranca.

Colecoes planejadas:

```text
users
listings
blocks
reports
jobs
reviews
```

Itens:

```text
- Criar usuarios online
- Criar anuncios online
- Buscar anuncios online
- Migrar ownerDeviceId para ownerId
- Migrar authorKey para userId/phone verificado
- Preparar geohash para busca por raio
```

## Fase regras de seguranca

Objetivo: impedir banco aberto e edicao indevida.

Regras desejadas:

```text
- Usuario so edita o proprio perfil
- Usuario so edita os proprios anuncios
- Denuncias sao criadas por usuario logado
- Bloqueios pertencem ao usuario logado
- Reviews so existem depois de job concluido
```

## Fase reputacao real

Objetivo: substituir toda reputacao visual/demo por reputacao real.

Fluxo correto:

```text
1. Usuario entra em contato
2. Job e criado ou confirmado
3. Contratante marca concluido
4. Trabalhador marca concluido
5. Avaliacao abre para os dois
6. Review vira reputacao publica
```

## Fase chat interno

Objetivo: adicionar conversa dentro do app somente depois de seguranca basica.

Antes do chat precisa existir:

```text
- Login
- Bloqueio
- Denuncia real
- Regras de seguranca
- Usuario real
```

## Fase monetizacao

Objetivo: monetizar sem matar adocao inicial.

Deixar para depois:

```text
- Anuncio destacado
- Plano profissional
- Selo pago
- Prioridade na busca
```

Nao monetizar antes de existir base ativa e confianca minima.
