# Schema Firestore - Chama no Trampo

Este documento define a estrutura planejada para migrar o app do modo local para Firebase/Firestore sem improvisar o banco depois.

## Principios

- O app e um marketplace bilateral: pessoas publicam demandas e profissionais publicam ofertas.
- Login real deve usar telefone verificado.
- Reputacao real so deve existir depois de servico concluido e confirmacao mutua.
- Denuncia real so existe quando houver backend recebendo o registro.
- Datas devem ser salvas como timestamp UTC, nunca como data local textual.

## users/{userId}

```text
id: string
phone: string
phoneVerified: boolean
displayName: string
city: string
kind: "trabalhador" | "contratante" | "empresa" | "misto"
createdAt: timestamp
updatedAt: timestamp
isBlocked: boolean
stats: {
  activeListings: number
  completedJobs: number
  reviewCount: number
  ratingAverage: number | null
}
```

Observacoes:

- `userId` vem do Firebase Auth.
- `phone` deve ser o telefone verificado pelo Firebase Auth Phone.
- Enquanto nao existir avaliacao real, `ratingAverage` deve ser `null`.

## listings/{listingId}

```text
id: string
ownerId: string
ownerPhone: string
ownerName: string
ownerCity: string
listingType: "demanda" | "oferta"
category: "Vaga" | "Bico" | "Servico"
urgent: boolean
title: string
place: string
city: string
lat: number | null
lng: number | null
geohash: string | null
value: string
description: string
status: "ativo" | "expirado" | "concluido" | "removido"
createdAt: timestamp
updatedAt: timestamp
expiresAt: timestamp
completedAt: timestamp | null
hiddenByModeration: boolean
```

Regras de expiracao planejadas:

```text
demanda urgente: 24 horas
demanda normal: 7 dias
oferta: 60 dias
```

Observacoes:

- `listingType` e campo estrutural. Nao substituir por categoria.
- `category` continua sendo Vaga/Bico/Servico.
- `urgent` e prioridade separada.
- `geohash` deve existir desde cedo para busca por raio futura.

## blocks/{blockId}

```text
id: string
blockerId: string
blockedUserId: string
blockedPhone: string
createdAt: timestamp
reason: string | null
```

Uso:

- Usuario bloqueado nao deve aparecer nas listas do bloqueador.
- No modo local atual, o equivalente e `authorKey` baseado em telefone.

## reports/{reportId}

```text
id: string
reporterId: string
listingId: string
reportedUserId: string
reason: string
comment: string | null
status: "aberto" | "em_analise" | "resolvido" | "ignorado"
createdAt: timestamp
resolvedAt: timestamp | null
```

Observacoes:

- Antes do Firestore, usar texto honesto: "Ocultar e marcar como suspeito".
- So chamar de denuncia quando este documento for criado no backend.

## jobs/{jobId}

Representa uma interacao real entre contratante e trabalhador.

```text
id: string
listingId: string
listingType: "demanda" | "oferta"
contractorId: string
workerId: string
status: "em_contato" | "combinado" | "concluido_por_contratante" | "concluido_por_trabalhador" | "concluido" | "cancelado"
createdAt: timestamp
updatedAt: timestamp
completedAt: timestamp | null
```

Observacoes:

- Avaliacao so deve abrir quando as duas partes confirmarem conclusao.

## reviews/{reviewId}

```text
id: string
jobId: string
listingId: string
fromUserId: string
toUserId: string
rating: number
comment: string | null
createdAt: timestamp
```

Regras:

- Criar review apenas se `jobs/{jobId}.status == "concluido"`.
- Um usuario nao pode avaliar sem participar do job.
- Uma parte so pode avaliar a outra uma vez por job.

## Migracao do modo local

Campos atuais equivalentes:

```text
ownerDeviceId -> ownerId
authorKey -> ownerId ou ownerPhone
phone -> ownerPhone
listingType -> listingType
category -> category
urgent -> urgent
createdAt -> createdAt
expiresAt -> expiresAt
status -> status
```

O modo local deve continuar funcionando para testes, mas a versao real deve exigir login para publicar, salvar, bloquear, ocultar e avaliar.
