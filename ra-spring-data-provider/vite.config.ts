import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    lib: {
      entry: resolve(__dirname, "src/index.ts"),
      name: "RaSpringDataProvider",
      formats: ["es", "umd"],
      fileName: (format) => `ra-spring-data-provider.${format}.js`,
    },
    rollupOptions: {
      external: ["react", "react-admin", "react-dom"],
      output: {
        globals: {
          react: "React",
          "react-admin": "ReactAdmin",
          "react-dom": "ReactDOM",
        },
      },
    },
  },
});
