import { Sparkles } from 'lucide-react';

export default function App() {
  return (
    <main className="min-h-screen bg-base-100 text-base-content">
      <div className="mx-auto flex min-h-screen max-w-3xl flex-col justify-center gap-6 px-6">
        <span className="badge badge-accent badge-outline w-fit gap-2">
          <Sparkles aria-hidden="true" className="size-3.5" />
          Ready to build
        </span>
        <h1 className="font-display text-5xl leading-tight tracking-tight">
          Your project is scaffolded.
        </h1>
        <p className="max-w-prose text-base-content/70">
          Describe what you want in the chat and the generated files will replace this screen.
        </p>
      </div>
    </main>
  );
}
