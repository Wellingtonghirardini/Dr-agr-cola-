# Dr. Agrícola — versão 7.1

Aplicativo Android em português para assistência e registros de manutenção agrícola.

A versão 7.1 usa a imagem do assistente como ícone do Android. Os recursos de ícone e o manifesto atualizado estão no projeto completo do ZIP.

## Recursos

- Chat com IA e contexto das últimas mensagens, com máquina selecionada.
- Fotos anexadas à pergunta e consulta de PDFs (até 8 MB).
- Ditado por reconhecimento de voz do Android e leitura das respostas.
- Conversas salvas, reabertura e compartilhamento.
- Cadastro e edição de máquinas, horímetro e lembretes de manutenção.
- Notificações de lembretes por data. O horímetro é atualizado manualmente e gera indicação no aplicativo.
- Biblioteca pessoal de PDFs e links HTTPS.
- Casos resolvidos pessoais com busca e compartilhamento.
- Exportação e restauração de backup JSON, incluindo PDFs.

Os dados ficam neste aparelho. Não há sincronização automática nem comunidade online nesta versão. A IA exige internet; voz e notificações dependem das permissões e dos serviços disponíveis no Android.

## Gerar o APK

No GitHub, abra Actions → Gerar APK Dr. Agrícola → Run workflow. Após a conclusão, baixe o artefato `Dr-Agricola-APK`, extraia o ZIP e instale `app-debug.apk`.

O workflow utiliza o projeto completo de `Dr_Agricola_V6_Android.zip` (nome legado; conteúdo versão 7.1) e substitui seu HTML pelo `index.html` da raiz. Ao alterar código nativo, manifesto ou recursos, atualize também o ZIP. A pasta `app` contém cópias para consulta.

## Servidor

`server.js` e `server/server.js` devem permanecer sincronizados. Instale as dependências do `package.json` e configure `OPENAI_API_KEY` apenas no servidor. `OPENAI_MODEL` é opcional. Nunca coloque a chave no APK ou no GitHub. Execute `npm start`.

`GET /` informa a versão e se a IA está configurada. `POST /chat` recebe `message`, `history`, `machine` e, opcionalmente, `image` (data URL) ou `file` (`name`, `data` PDF base64).

## Verificação no celular

1. Cadastre uma máquina e selecione-a no chat.
2. Envie texto, foto e uma pergunta sobre um PDF.
3. Use Falar, confira a transcrição e envie; toque em Ouvir na resposta.
4. Feche e reabra o aplicativo para conferir a persistência.
5. Agende uma manutenção e permita notificações.
6. Registre um caso e exporte/restaure um backup.

A compilação não substitui a verificação de microfone, leitura por voz e notificações em aparelho Android real.
