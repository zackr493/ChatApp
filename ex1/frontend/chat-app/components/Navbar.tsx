"use client";

import { usePathname, useRouter } from "next/navigation";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import ChatIcon from "@mui/icons-material/Chat";
import DashboardIcon from "@mui/icons-material/Dashboard";

export default function Navbar() {
  const router = useRouter();
  const pathname = usePathname();

  return (
    <AppBar
      position="static"
      elevation={0}
      sx={{
        borderBottom: "1px solid rgba(255,255,255,0.06)",
        bgcolor: "#12121A",
      }}
    >
      <Toolbar className="gap-4">
        <Typography
          variant="h6"
          sx={{
            fontFamily: '"DM Mono", monospace',
            color: "primary.main",
            mr: 2,
            cursor: "pointer",
          }}
          onClick={() => router.push("/")}
        >
          chat-app
        </Typography>
        <Button
          startIcon={<ChatIcon />}
          onClick={() => router.push("/")}
          variant={pathname === "/" ? "contained" : "text"}
          size="small"
        >
          Client
        </Button>
        <Button
          startIcon={<DashboardIcon />}
          onClick={() => router.push("/admin")}
          variant={pathname === "/admin" ? "contained" : "text"}
          size="small"
        >
          Admin
        </Button>
      </Toolbar>
    </AppBar>
  );
}
