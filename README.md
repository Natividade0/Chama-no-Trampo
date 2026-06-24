# Chama no Trampo

Aplicativo Android para conectar pessoas, profissionais autonomos e empresas por meio de oportunidades locais.

## Proposta

O **Chama no Trampo** sera um app para divulgar e encontrar:

- Vagas de emprego
- Bicos rapidos
- Servicos residenciais
- Servicos para empresas
- Demandas urgentes para hoje

A ideia central e simples:

> Quem precisa publica. Quem faz chama.

## MVP atual

Versao nativa Android criada pelo celular, com visual premium, oportunidades locais, publicacao, perfil local, contato por WhatsApp, negociacao local por proposta, avaliacao apos conclusao, busca funcional, favoritos, avisos internos e tela de detalhes.

Funcionalidades atuais:

- Tela inicial com identidade do app
- Cards de oportunidades mais limpos na home
- Tela de detalhes da oportunidade
- Filtro por categoria
- Busca funcional por titulo, cidade, valor, descricao, autor e categoria
- Sugestoes rapidas de busca
- Favoritos locais por oportunidade
- Tela de favoritos mantendo o usuario no fluxo correto
- Central de avisos internos com texto explicando que ainda nao e push real
- Avisos para favoritos, mensagens, conclusoes, avaliacoes e publicacoes
- Formulario para publicar oportunidade
- ID local fixo para novas oportunidades
- Perfil local do usuario
- Oportunidades salvas no aparelho
- Chat local por proposta
- Historico de mensagens da negociacao
- Mensagens rapidas para combinar detalhes
- Status visual da conversa
- Avaliacao local apos concluir servico
- Nota de 1 a 5 com comentario
- Exibicao da avaliacao nos detalhes e na conversa
- Botao para contato via WhatsApp
- Redesign visual com cards premium, chips e bottom nav

## Categorias iniciais

- Vaga de emprego
- Bico rapido
- Servico
- Urgente / Preciso para hoje
- Seguranca do Trabalho

## Fluxo de negociacao

Proposta -> Detalhes -> Conversa -> Combinado -> Concluido -> Avaliacao

## Recursos locais adicionados

- Busca dentro das oportunidades salvas
- Favoritos salvos no aparelho
- Avisos internos salvos no aparelho
- ID local para novas oportunidades

## Refatoracao atual

A `MainActivity.java` foi reescrita em formato legivel, com metodos separados por responsabilidade:

- carregamento e salvamento local;
- home e filtros;
- busca;
- detalhes da oportunidade;
- favoritos;
- avisos internos;
- conversa;
- avaliacao;
- publicacao;
- perfil;
- componentes visuais reutilizaveis.

O botao/gesto de voltar do Android tambem foi organizado por tela:

- na Home, volta fecha o app;
- em Detalhes, volta retorna para a origem correta;
- no Chat, volta retorna para Detalhes;
- em Avaliacao, volta retorna para Chat;
- em Resultado de busca, volta retorna para a tela de busca;
- nas demais telas, volta retorna para Home.

## Proximas etapas

- Teste manual no AndroidIDE
- Ajustes finos de layout em tela pequena
- Ranking e reputacao do perfil
- Integracao com Firebase Auth
- Cadastro de usuario/profissional/empresa
- Firestore para salvar oportunidades, conversas, favoritos, avisos e avaliacoes reais
- Anuncios destacados
- Notificacoes push reais

## Stack inicial

- Android nativo
- Java
- Gradle
- AndroidIDE
