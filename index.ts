// Supabase Edge Function: text generation only.
// Configure GEMINI_API_KEY as a server-side secret. Never place it in the Android app.
import { serve } from "https://deno.land/std@0.224.0/http/server.ts";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return new Response(JSON.stringify({ error: "POST required" }), {
    status: 405, headers: { ...cors, "Content-Type": "application/json" },
  });

  try {
    const apiKey = Deno.env.get("GEMINI_API_KEY");
    if (!apiKey) throw new Error("GEMINI_API_KEY secret is missing");
    const body = await req.json();
    const idea = String(body.idea ?? "").trim().slice(0, 4000);
    const platforms = Array.isArray(body.platforms)
      ? body.platforms.filter((x: unknown) => typeof x === "string").slice(0, 4)
      : [];
    const tone = String(body.tone ?? "Emotivo y motivador").slice(0, 100);
    const formats = String(body.formats ?? "Todos los formatos").slice(0, 100);
    if (!idea || !platforms.length) {
      return new Response(JSON.stringify({ error: "idea and platforms are required" }), {
        status: 400, headers: { ...cors, "Content-Type": "application/json" },
      });
    }

    const prompt = `
Eres el motor creativo de MADDY, una aplicación de historias originales y motivación.
Usa esta idea como punto de partida, no copies frases conocidas. No afirmes que la historia es real si no se indicó.
Estilo: ${tone}. Formatos solicitados: ${formats}. Plataformas: ${platforms.join(", ")}.
Idea del usuario: ${idea}

Devuelve SOLO JSON válido con esta estructura:
{"drafts":[{"platform":"nombre exacto de plataforma","kind":"Historia / publicación | Texto para imagen | Guion para video vertical","text":"contenido"}]}
Crea una o más piezas apropiadas para cada plataforma seleccionada. Para guiones de video incluye escenas, narración y sugerencias de subtítulos.
No incluyas campos extra. Evita hashtags engañosos y marcas de terceros.
`;
    const aiResponse = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + encodeURIComponent(apiKey),
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ role: "user", parts: [{ text: prompt }] }],
          generationConfig: { responseMimeType: "application/json", temperature: 0.9 },
        }),
      },
    );
    const aiData = await aiResponse.json();
    if (!aiResponse.ok) {
      return new Response(JSON.stringify({ error: "AI provider error", detail: aiData }), {
        status: 502, headers: { ...cors, "Content-Type": "application/json" },
      });
    }
    const text = aiData?.candidates?.[0]?.content?.parts?.map((p: {text?: string}) => p.text ?? "").join("");
    if (!text) throw new Error("AI returned an empty response");
    const parsed = JSON.parse(text);
    if (!Array.isArray(parsed.drafts)) throw new Error("AI response has no drafts array");
    const allowed = new Set(platforms);
    parsed.drafts = parsed.drafts.filter((d: {platform?: string; kind?: string; text?: string}) =>
      allowed.has(d.platform ?? "") && typeof d.kind === "string" && typeof d.text === "string"
    ).slice(0, 20);
    return new Response(JSON.stringify(parsed), {
      headers: { ...cors, "Content-Type": "application/json" },
    });
  } catch (err) {
    return new Response(JSON.stringify({ error: String(err) }), {
      status: 500, headers: { ...cors, "Content-Type": "application/json" },
    });
  }
});
