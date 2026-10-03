import express from "express";
import OpenAI from "openai";

const app = express();

app.use(express.json());

/*
  Permite que o aplicativo Dr. Agrícola
  converse com este servidor.
*/
app.use((req, res, next) => {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  res.header("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.sendStatus(204);
  }

  next();
});

const client = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY
});


/*
  Teste do servidor
*/
app.get("/", (req, res) => {
  res.json({
    status: "online",
    aplicativo: "Dr. Agrícola",
    servidor: "funcionando"
  });
});


/*
  Chat do Dr. Agrícola
*/
app.post("/chat", async (req, res) => {

  try {

    const mensagem =
    req.body?.pergunta ||
    req.body?.message ||
    req.body?.mensagem;

    if (!mensagem || typeof mensagem !== "string") {

      return res.status(400).json({
        error: "Mensagem não informada."
      });

    }


    const resposta = await client.responses.create({

      model: "gpt-5.6-luna",

      instructions: `
Você é o Dr. Agrícola, um assistente especializado
em máquinas e equipamentos agrícolas.

Você atende principalmente:

- mecânicos;
- operadores;
- proprietários de máquinas;
- produtores rurais.

Você pode ajudar com:

- tratores;
- motores;
- transmissões;
- sistemas hidráulicos;
- sistemas elétricos;
- sensores;
- módulos eletrônicos;
- solenoides;
- bombas;
- válvulas;
- manutenção;
- diagnóstico de falhas;
- procedimentos de teste;
- peças e componentes.

Responda sempre em português do Brasil.

Se a pergunta depender do modelo da máquina,
peça marca, modelo, ano e número de série quando
essas informações forem importantes.

Nunca invente:

- códigos de peças;
- pressões;
- torques;
- valores elétricos;
- especificações técnicas.

Quando não tiver certeza, diga claramente.

Quando orientar testes elétricos ou mecânicos,
inclua os cuidados de segurança necessários.

Quando uma pergunta precisar de informação atual,
pesquise na internet antes de responder e deixe claro
quando uma informação vier de uma fonte externa.

Dê respostas práticas e organizadas,
preferencialmente em passos numerados.
`,

      tools: [
        {
          type: "web_search"
        }
      ],

      input: mensagem

    });


    res.json({
    resposta: resposta.output_text
});


  } catch (erro) {

    console.error("ERRO OPENAI:", erro);

    res.status(500).json({

      error:
        "Erro ao consultar a inteligência artificial.",

      details:
        erro?.message || "Erro desconhecido"

    });

  }

});


const PORT =
  process.env.PORT || 3000;


app.listen(
  PORT,
  "0.0.0.0",
  () => {

    console.log(
      `Dr. Agrícola online na porta ${PORT}`
    );

  }
);
