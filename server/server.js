import express from "express";
import OpenAI from "openai";

const app = express();

app.use(express.json());

const client = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY
});

app.get("/", (req, res) => {
  res.json({
    status: "online",
    aplicativo: "Dr. Agrícola"
  });
});

app.post("/chat", async (req, res) => {
  try {
    const mensagem = req.body?.message;

    if (!mensagem || typeof mensagem !== "string") {
      return res.status(400).json({
        error: "Mensagem não informada."
      });
    }

    const resposta = await client.responses.create({
      model: "gpt-5.6-luna",
      instructions: `
Você é o Dr. Agrícola, um assistente especializado em máquinas agrícolas.

Ajude mecânicos, operadores e proprietários de máquinas agrícolas.

Você pode ajudar com:
- tratores;
- motores;
- transmissões;
- sistemas hidráulicos;
- sistemas elétricos;
- sensores;
- solenoides;
- manutenção;
- diagnóstico de falhas;
- peças e componentes.

Responda sempre em português do Brasil.

Se a pergunta depender do modelo exato da máquina,
peça marca, modelo, ano e número de série quando necessário.

Nunca invente especificações técnicas, códigos de peças,
pressões, torques ou valores elétricos.

Quando não tiver certeza, deixe isso claro.

Ao orientar testes mecânicos ou elétricos,
explique os cuidados de segurança necessários.
      `,
      input: mensagem
    });

    res.json({
      answer: resposta.output_text
    });

  } catch (erro) {
    console.error(erro);

    res.status(500).json({
      error: "Erro ao consultar o Dr. Agrícola."
    });
  }
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Dr. Agrícola online na porta ${PORT}`);
});
