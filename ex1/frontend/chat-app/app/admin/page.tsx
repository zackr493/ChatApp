"use client";

import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Rating,
  Button,
  Tabs,
  Tab,
  Paper,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stack,
  useRadioGroup,
} from "@mui/material";
import axios from "axios";
import { useRouter } from "next/navigation";

const serverUrl = "http://localhost:8080";

interface Server {
  id: string;
  serverName: string;
  host: string;
  numClientsDay: number;
  numClientsMonth: number;
  averageRating: number;
  lastHeartbeatAt: string | null;
}
interface Session {
  id: string;
  status: string;
  clientEntity: { clientName: string } | null;
  serverEntity: { serverName: string } | null;
  startTime: string | null;
  endTime: string | null;
  rating: number;
}

interface LostClient {
  id: string;
  clientId: string;
  createdAt: string;
}

interface Message {
  id: string;
  role: "USER" | "ASSISTANT";
  content: string;
  sentAt: string;
}

export default function AdminPage() {
  const router = useRouter();
  const [tab, setTab] = useState(0);
  const [servers, setServers] = useState<Server[]>([]);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [lost, setLost] = useState<LostClient[]>([]);
  const [activeClients, setActiveClients] = useState<Record<string, string[]>>(
    {},
  );
  const [loading, setLoading] = useState(false);
  const [selectedSession, setSelectedSession] = useState<Session | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [msgLoading, setMsgLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [serverData, sessionsData, lostClientsData] = await Promise.all([
        axios.get(`${serverUrl}/servers`).catch((err) => {
          if (err.response?.status === 404) {
            return { data: { data: [] } };
          }
          throw err;
        }),
        axios.get(`${serverUrl}/sessions`).catch((err) => {
          if (err.response?.status === 404) {
            return { data: { data: [] } };
          }
          throw err;
        }),
        axios.get(`${serverUrl}/lost-clients`).catch((err) => {
          if (err.response?.status === 404) {
            return { data: { data: [] } };
          }
          throw err;
        }),
      ]);

      const serverList: Server[] = serverData.data.data ?? [];
      setServers(serverList);
      setSessions(sessionsData.data.data ?? []);

      setLost(lostClientsData.data.data ?? []);

      const map: Record<string, string[]> = {};

      await Promise.allSettled(
        serverList.map(async (server) => {
          try {
            const r = await axios.get(
              `${serverUrl}/servers/${server.id}/active-clients`,
            );
            map[server.id] = r.data.data ?? [];
          } catch {
            map[server.id] = [];
          }
        }),
      );

      setActiveClients(map);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const openSession = (s: Session) => {
    router.push(
      `/?sessionId=${s.id}&clientName=${encodeURIComponent(s.clientEntity?.clientName ?? "")}`,
    );
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <Box>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h6">Admin</Typography>
        <Typography variant="body2" color="text.secondary">
          Servers: {servers.length} · Sessions: {sessions.length} · Lost:{" "}
          {lost.length}
        </Typography>
        <Button
          size="small"
          variant="outlined"
          onClick={load}
          disabled={loading}
        >
          {loading ? <CircularProgress size={14} /> : "Refresh"}
        </Button>
      </Stack>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label={`Servers (${servers.length})`} />
        <Tab label={`Sessions (${sessions.length})`} />
        <Tab label={`Lost (${lost.length})`} />
      </Tabs>

      {tab === 0 && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Host</TableCell>
                <TableCell>Today</TableCell>
                <TableCell>Month</TableCell>
                <TableCell>Avg Rating</TableCell>
                <TableCell>Active Clients</TableCell>
                <TableCell>Last Heartbeat</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {servers.map((s) => (
                <TableRow key={s.id}>
                  <TableCell>{s.serverName}</TableCell>
                  <TableCell>
                    <Typography variant="caption" fontFamily="monospace">
                      {s.host}
                    </Typography>
                  </TableCell>
                  <TableCell>{s.numClientsDay}</TableCell>
                  <TableCell>{s.numClientsMonth}</TableCell>
                  <TableCell>
                    <Stack direction="row" alignItems="center" spacing={0.5}>
                      <Rating
                        value={s.averageRating}
                        readOnly
                        size="small"
                        precision={0.1}
                      />
                      <Typography variant="caption">
                        ({s.averageRating.toFixed(1)})
                      </Typography>
                    </Stack>
                  </TableCell>
                  <TableCell>
                    {activeClients[s.id]?.length ? (
                      activeClients[s.id].join(", ")
                    ) : (
                      <Typography variant="caption" color="text.secondary">
                        none
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <Typography variant="caption">
                      {s.lastHeartbeatAt
                        ? new Date(s.lastHeartbeatAt).toLocaleString()
                        : "—"}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {tab === 1 && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Client</TableCell>
                <TableCell>Server</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Rating</TableCell>
                <TableCell>Started</TableCell>
                <TableCell>Ended</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {sessions.map((s) => (
                <TableRow
                  key={s.id}
                  hover
                  sx={{ cursor: "pointer" }}
                  onClick={() => openSession(s)}
                >
                  <TableCell>
                    <Typography variant="caption" fontFamily="monospace">
                      {s.id.slice(0, 8)}...
                    </Typography>
                  </TableCell>
                  <TableCell>{s.clientEntity?.clientName ?? "—"}</TableCell>
                  <TableCell>{s.serverEntity?.serverName ?? "—"}</TableCell>
                  <TableCell>
                    <Chip label={s.status} size="small" />
                  </TableCell>
                  <TableCell>
                    {s.rating > 0 ? (
                      <Rating value={s.rating} readOnly size="small" />
                    ) : (
                      "—"
                    )}
                  </TableCell>
                  <TableCell>
                    <Typography variant="caption">
                      {s.startTime
                        ? new Date(s.startTime).toLocaleTimeString()
                        : "—"}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="caption">
                      {s.endTime
                        ? new Date(s.endTime).toLocaleTimeString()
                        : "—"}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {tab === 2 && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Client ID</TableCell>
                <TableCell>Lost At</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lost.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={2}>
                    <Typography variant="body2" color="text.secondary">
                      None
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                lost.map((lc) => (
                  <TableRow key={lc.id}>
                    <TableCell>
                      <Typography variant="caption" fontFamily="monospace">
                        {lc.clientId}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="caption">
                        {new Date(lc.createdAt).toLocaleString()}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog
        open={!!selectedSession}
        onClose={() => setSelectedSession(null)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {selectedSession?.clientEntity?.clientName} —{" "}
          {selectedSession?.serverEntity?.serverName ?? "—"}
          <Chip label={selectedSession?.status} size="small" sx={{ ml: 1 }} />
        </DialogTitle>
        <DialogContent dividers>
          {msgLoading ? (
            <CircularProgress size={20} />
          ) : messages.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              No messages
            </Typography>
          ) : (
            messages.map((m) => (
              <Box
                key={m.id}
                sx={{ mb: 1, textAlign: m.role === "USER" ? "right" : "left" }}
              >
                <Typography
                  variant="caption"
                  color="text.secondary"
                  display="block"
                >
                  {m.role} · {new Date(m.sentAt).toLocaleTimeString()}
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
            ))
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSelectedSession(null)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
