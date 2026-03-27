"use client";

import { useState, useRef, useEffect } from "react";
import {
  Box,
  TextField,
  Button,
  Typography,
  Paper,
  Stack,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Rating,
} from "@mui/material";

import { useSearchParams } from "next/navigation";

import axios from "axios";

const serverUrl = "http://localhost:8080";
type Stage = "NAME" | "CHATTING" | "RATING" | "DONE";
interface Message {
  role: "USER" | "ASSISTANT";
  content: string;
}

export default function ClientPage() {
  const [stage, setStage] = useState<Stage>("NAME");
  const [clientName, setClientName] = useState("");
  const [clientId, setClientId] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [serverName, setServerName] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [rating, setRating] = useState(0);
  const [now, setNow] = useState(new Date());

  const searchParams = useSearchParams();

  useEffect(() => {
    const sid = searchParams.get("sessionId");
    const name = searchParams.get("clientName");

    if (sid && name) {
      setClientName(name);
      setSessionId(sid);
      setStage("CHATTING");

      // fetch session, messages in parallel
      Promise.all([
        axios.get(`${serverUrl}/sessions/${sid}`),
        axios.get(`${serverUrl}/messages/session/${sid}`),
      ])
        .then(([sessionRes, messagesRes]) => {
          setClientId(sessionRes.data?.data?.clientEntity?.id ?? null);
          setServerName(
            sessionRes.data?.data?.serverEntity?.serverName ?? null,
          );
          setMessages(
            (messagesRes.data.data ?? []).map((m: any) => ({
              role: m.role,
              content: m.content,
            })),
          );
        })
        .catch(() => {});
    }
  }, []);

  const start = async () => {
    if (!clientName.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const r = await axios.post(`${serverUrl}/clients`, {
        clientName: clientName.trim(),
      });
      setClientId(r.data.data.id);
      setStage("CHATTING");
    } catch {
      setError("Failed to connect");
    } finally {
      setLoading(false);
    }
  };

  const send = async () => {
    if (!input.trim() || loading || !clientId) return;
    const content = input.trim();
    setInput("");
    setLoading(true);
    setError(null);
    setMessages((p) => [...p, { role: "USER", content }]);
    try {
      const r = await axios.post(`${serverUrl}/messages/send`, {
        clientId,
        sessionId: sessionId ?? null,
        content,
      });
      const { sessionId: sid, reply } = r.data.data;
      if (!sessionId) setSessionId(sid);
      setMessages((p) => [...p, { role: "ASSISTANT", content: reply }]);
    } catch {
      setError("Failed to send");
      setMessages((p) => p.slice(0, -1));
    } finally {
      setLoading(false);
    }
  };

  const finish = async () => {
    if (!sessionId) return;
    setLoading(true);
    try {
      await axios.post(`${serverUrl}/sessions/finish`, { sessionId, rating });
      setStage("DONE");
    } catch {
      setError("Failed to finish");
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setStage("NAME");
    setClientName("");
    setClientId(null);
    setSessionId(null);
    setServerName(null);
    setMessages([]);
    setInput("");
    setRating(0);
    setError(null);
  };

  if (stage === "NAME")
    return (
      <Box sx={{ maxWidth: 400 }}>
        <Typography variant="h6" gutterBottom>
          Start Chat
        </Typography>
        <TextField
          fullWidth
          size="small"
          label="Your name"
          value={clientName}
          autoFocus
          onChange={(e) => setClientName(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && start()}
          disabled={loading}
          sx={{ mb: 2 }}
        />
        {error && (
          <Typography color="error" variant="body2" sx={{ mb: 1 }}>
            {error}
          </Typography>
        )}
        <Button
          variant="contained"
          onClick={start}
          disabled={loading || !clientName.trim()}
          disableElevation
        >
          {loading ? <CircularProgress size={18} /> : "Connect"}
        </Button>
      </Box>
    );

  if (stage === "DONE")
    return (
      <Box sx={{ maxWidth: 400 }}>
        <Typography variant="h6" gutterBottom>
          Session ended
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Thanks {clientName}.
        </Typography>
        <Button variant="outlined" onClick={reset}>
          Start again
        </Button>
      </Box>
    );

  return (
    <Box sx={{ maxWidth: 640 }}>
      {/* Info bar */}
      <Stack direction="row" spacing={2} sx={{ mb: 1 }} alignItems="center">
        <Typography variant="body2">
          <strong>Client:</strong> {clientName}
        </Typography>
        <Typography variant="body2">
          <strong>Server:</strong> {serverName ?? "—"}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {now.toLocaleTimeString()}
        </Typography>
        <Box sx={{ flex: 1 }} />
        <Button
          size="small"
          color="error"
          variant="outlined"
          onClick={() => setStage("RATING")}
          disabled={!sessionId || loading}
        >
          Finish
        </Button>
      </Stack>

      <Paper
        variant="outlined"
        sx={{ height: 400, overflowY: "auto", p: 2, mb: 1 }}
      >
        {messages.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No messages yet.
          </Typography>
        )}

        {messages.map((m, i) => (
          <Box
            key={i}
            sx={{ mb: 1, textAlign: m.role === "USER" ? "right" : "left" }}
          >
            <Typography
              variant="caption"
              color="text.secondary"
              display="block"
            >
              {m.role === "USER" ? clientName : (serverName ?? "Server")}
            </Typography>
            <Typography
              variant="body2"
              sx={{
                display: "inline-block",
                bgcolor: m.role === "USER" ? "primary.light" : "grey.100",
                px: 1.5,
                py: 0.5,
                borderRadius: 1,
              }}
            >
              {m.content}
            </Typography>
          </Box>
        ))}
        {loading && (
          <Typography variant="caption" color="text.secondary">
            ...
          </Typography>
        )}
      </Paper>

      {error && (
        <Typography color="error" variant="caption">
          {error}
        </Typography>
      )}

      <Stack direction="row" spacing={1}>
        <TextField
          fullWidth
          size="small"
          placeholder="Type a message..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && send()}
          disabled={loading}
        />
        <Button
          variant="contained"
          onClick={send}
          disabled={loading || !input.trim()}
          disableElevation
        >
          Send
        </Button>
      </Stack>

      <Dialog open={stage === "RATING"} maxWidth="xs" fullWidth>
        <DialogTitle>Rate this session</DialogTitle>
        <DialogContent>
          <Rating value={rating} onChange={(_, v) => setRating(v ?? 0)} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStage("CHATTING")}>Back</Button>
          <Button
            variant="contained"
            onClick={finish}
            disabled={!rating || loading}
            disableElevation
          >
            Submit
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
