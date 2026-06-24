# Auditoria do app Chama no Trampo

Auditoria estatica feita sobre a branch `busca-favoritos-notificacoes`.

## Resumo executivo

O app ja tem um MVP local interessante:

- oportunidades locais;
- publicacao local;
- perfil local;
- chat local por proposta;
- status da negociacao;
- avaliacao apos conclusao;
- busca funcional;
- favoritos;
- avisos internos;
- saida para WhatsApp.

A prioridade agora nao deveria ser adicionar mais funcionalidades. O mais importante e estabilizar a experiencia, limpar o fluxo e preparar o projeto para crescer sem quebrar.

## Pontos positivos encontrados

- O projeto continua simples e compativel com Android nativo em Java.
- A persistencia local usa `SharedPreferences`, adequada para MVP sem backend.
- O fluxo principal esta claro: `Proposta -> Conversa -> Combinado -> Concluido -> Avaliacao`.
- A busca ja procura por titulo, local, valor, descricao, autor e categoria.
- Favoritos e avisos internos ja funcionam como recursos locais.
- O WhatsApp continua como saida final, sem travar o MVP em chat real ainda.

## Riscos e problemas encontrados

### 1. MainActivity esta grande demais

Quase todo o app esta dentro de um unico arquivo `MainActivity.java`.

Risco:

- qualquer nova funcao aumenta chance de quebrar outra tela;
- fica dificil testar;
- fica dificil migrar para Firebase depois;
- fica dificil manter visual consistente.

Acao recomendada:

- nao adicionar funcionalidades grandes antes de organizar o codigo;
- separar gradualmente modelos, persistencia local e telas.

### 2. Cards de oportunidade estao poluidos

O card principal agora concentra muita coisa:

- dados da oportunidade;
- status da conversa;
- avaliacao;
- favorito;
- abrir conversa;
- WhatsApp.

Risco:

- tela inicial fica pesada;
- usuario se perde;
- visual parece menos profissional conforme adiciona recursos.

Acao recomendada:

- criar tela de detalhes da oportunidade;
- deixar o card da home mais limpo;
- mover botoes secundarios para detalhes.

### 3. Favoritos podem jogar o usuario para a home

O botao de favorito dentro do card chama a tela principal depois de alternar favorito.

Risco:

- quando o usuario remove um item dentro da tela Favoritos, ele pode sair da tela e voltar para a home;
- experiencia parece quebrada.

Acao recomendada:

- criar uma versao do card que recebe a tela de retorno;
- em Favoritos, apos remover, permanecer na tela Favoritos.

### 4. Busca funcional existe, mas ainda nao e fluida

A busca funciona por botao, mas nao filtra enquanto digita.

Risco:

- parece menos moderna;
- exige mais toques;
- usuario pode achar que a busca nao e instantanea.

Acao recomendada:

- manter o botao por enquanto se quiser simplicidade;
- ou adicionar busca ao pressionar Enter/IME action depois;
- no futuro, usar TextWatcher para filtro em tempo real.

### 5. Notificacoes internas nao sao push real

A central de avisos registra eventos locais, mas nao envia notificacao do Android.

Risco:

- usuario pode entender como notificacao real;
- sem Firebase/WorkManager, o app nao avisa fora da tela.

Acao recomendada:

- renomear visualmente para `Avisos internos` ou `Historico de avisos`;
- deixar push real para fase Firebase.

### 6. Chaves locais dependem dos dados da proposta

Conversas, status, favoritos e avaliacao usam uma chave derivada de titulo, local, autor e contato.

Risco:

- se uma oportunidade for editada no futuro, historico pode se perder;
- duas oportunidades muito parecidas podem causar confusao;
- migrar para banco real ficara mais dificil sem ID.

Acao recomendada:

- adicionar `id` local fixo em cada oportunidade nova;
- preservar compatibilidade com oportunidades antigas.

### 7. Ainda nao existe edicao/exclusao de oportunidade

O app permite publicar, mas nao apagar ou editar.

Risco:

- lista local fica suja;
- usuario nao consegue corrigir erro de publicacao;
- durante testes, fica acumulando oportunidade.

Acao recomendada:

- adicionar acao de excluir oportunidade;
- adicionar editar depois, se necessario.

### 8. PRs estao empilhados

Ha uma sequencia de PRs dependentes:

- chat local;
- avaliacao local;
- busca/favoritos/avisos.

Risco:

- se mergear fora de ordem, o historico fica confuso;
- `main` ainda pode estar atrasada em relacao ao app testado no celular;
- novas branches podem nascer da base errada.

Acao recomendada:

- mergear na ordem correta;
- ou consolidar tudo em uma branch final antes de continuar.

### 9. Build Android real ainda precisa ser validado no AndroidIDE

A auditoria foi estatica. Nao houve build Android real neste ambiente.

Risco:

- pode haver erro especifico do AndroidIDE/Gradle;
- pode haver problema visual em tela pequena;
- pode haver comportamento diferente no aparelho.

Acao recomendada:

- rodar `sh gradlew clean`;
- abrir o app;
- testar o fluxo completo manualmente.

## Ordem recomendada de trabalho

### Prioridade 1 - Estabilizacao

- corrigir retorno dos favoritos;
- renomear/clarear avisos internos;
- revisar fluxo de voltar;
- reduzir poluicao dos cards.

### Prioridade 2 - Organizacao visual

- criar tela de detalhes da oportunidade;
- home com cards menores;
- detalhes com botoes completos.

### Prioridade 3 - Dados locais melhores

- adicionar ID local fixo para oportunidades novas;
- preparar migracao futura para Firestore;
- adicionar excluir oportunidade.

### Prioridade 4 - Backend real

- Firebase Auth;
- Firestore;
- usuarios reais;
- notificacoes push reais.

## Proxima branch recomendada

Nome sugerido:

`estabilizacao-fluxo-local`

Objetivo:

- nao adicionar recurso novo;
- corrigir UX dos favoritos;
- ajustar textos de avisos internos;
- preparar tela de detalhes;
- reduzir risco antes de Firebase.

## Conclusao

O app esta evoluindo bem para MVP. A melhor decisao agora e parar de empilhar funcionalidades e fazer uma rodada de estabilizacao. A proxima entrega deveria ser de limpeza de fluxo e experiencia, nao uma funcao nova.
