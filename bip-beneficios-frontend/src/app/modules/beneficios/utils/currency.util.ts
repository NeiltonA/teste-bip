const brlFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

export function formatBrl(value: number): string {
  return brlFormatter.format(Number.isFinite(value) ? value : 0);
}

export function parseBrlInput(display: string): number {
  const digits = display.replace(/\D/g, '');
  if (!digits) {
    return 0;
  }
  return Number(digits) / 100;
}

export function maskBrlFromInput(raw: string): string {
  return formatBrl(parseBrlInput(raw));
}
