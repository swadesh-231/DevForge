package com.devforge.ai.prompt;

public final class ProjectRuntimeManifest {

    public static final String REACT_VERSION = "19.2.8";
    public static final String TYPESCRIPT_VERSION = "7.0.2";
    public static final String VITE_VERSION = "8.2.2";
    public static final String VITE_PLUGIN_REACT_VERSION = "6.1.1";
    public static final String TAILWIND_VERSION = "4.3.3";
    public static final String DAISYUI_VERSION = "5.7.22";
    public static final String LUCIDE_VERSION = "1.38.0";
    public static final String MOTION_VERSION = "13.1.1";

    public static final String PACKAGE_JSON = """
            {
              "name": "devforge-app",
              "private": true,
              "version": "0.0.0",
              "type": "module",
              "scripts": {
                "dev": "vite",
                "build": "tsc --noEmit && vite build",
                "preview": "vite preview"
              },
              "dependencies": {
                "react": "%s",
                "react-dom": "%s",
                "lucide-react": "%s",
                "motion": "%s"
              },
              "devDependencies": {
                "@tailwindcss/vite": "%s",
                "@types/react": "19.2.x",
                "@types/react-dom": "19.2.x",
                "@vitejs/plugin-react": "%s",
                "daisyui": "%s",
                "tailwindcss": "%s",
                "typescript": "%s",
                "vite": "%s"
              }
            }"""
            .formatted(
                    REACT_VERSION,
                    REACT_VERSION,
                    LUCIDE_VERSION,
                    MOTION_VERSION,
                    TAILWIND_VERSION,
                    VITE_PLUGIN_REACT_VERSION,
                    DAISYUI_VERSION,
                    TAILWIND_VERSION,
                    TYPESCRIPT_VERSION,
                    VITE_VERSION);

    private ProjectRuntimeManifest() {
    }
}
