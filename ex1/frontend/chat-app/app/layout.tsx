"use client";

import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";
import { useRouter, usePathname } from "next/navigation";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();

  return (
    <html lang="en">
      <body>
        <AppBar position="static" color="default" elevation={1}>
          <Toolbar variant="dense">
            <Typography variant="subtitle1" sx={{ mr: 3, fontWeight: 600 }}>
              Chat App
            </Typography>
            <Button
              size="small"
              onClick={() => router.push("/")}
              variant={pathname === "/" ? "contained" : "text"}
              disableElevation
            >
              Client
            </Button>
            <Button
              size="small"
              onClick={() => router.push("/admin")}
              variant={pathname === "/admin" ? "contained" : "text"}
              disableElevation
              sx={{ ml: 1 }}
            >
              Admin
            </Button>
          </Toolbar>
        </AppBar>
        <Box component="main" sx={{ p: 3 }}>
          {children}
        </Box>
      </body>
    </html>
  );
}
