export interface Beneficio {
  id: number;
  nome: string;
  descricao: string | null;
  valor: number;
  ativo: boolean;
  version: number | null;
}

export interface BeneficioPayload {
  nome: string;
  descricao: string | null;
  valor: number;
  ativo: boolean;
}

export interface TransferenciaPayload {
  origemId: number;
  destinoId: number;
  valor: number;
}

export interface TransferenciaResultado {
  origemId: number;
  destinoId: number;
  valorTransferido: number;
  saldoOrigem: number;
  saldoDestino: number;
}
