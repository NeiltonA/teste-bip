import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Subscription, finalize, take } from 'rxjs';
import {
  Beneficio,
  BeneficioPayload,
  TransferenciaPayload,
} from '../../models/beneficio.model';
import { BeneficioService } from '../../services/beneficio.service';
import { formatBrl, maskBrlFromInput, parseBrlInput } from '../../utils/currency.util';

type StatusFilter = 'all' | 'active' | 'inactive';

const LIST_SAFETY_MS = 13_000;
const TOAST_DURATION_MS = 3000;
const TOAST_FADE_MS = 400;

type ToastType = 'success' | 'error';

interface ToastState {
  text: string;
  type: ToastType;
}

const nonEmptyTrimmed: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = String(control.value ?? '').trim();
  return value.length > 0 ? null : { required: true };
};

@Component({
  selector: 'app-beneficio-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './beneficio-page.component.html',
  styleUrl: './beneficio-page.component.scss',
})
export class BeneficioPageComponent implements OnInit, OnDestroy {
  beneficios: Beneficio[] = [];
  selectedId: number | null = null;
  highlightedId: number | null = null;
  searchTerm = '';
  statusFilter: StatusFilter = 'all';

  cadastroModalOpen = false;
  transferModalOpen = false;
  confirmDeleteBeneficio: Beneficio | null = null;

  beneficioValorDisplay = formatBrl(0);
  transferenciaValorDisplay = formatBrl(0);

  initialLoading = false;
  refreshingList = false;
  listLoadFailed = false;
  saving = false;
  transferring = false;
  deletingId: number | null = null;

  error = '';
  toast: ToastState | null = null;
  toastLeaving = false;

  private listSub?: Subscription;
  private listSafetyTimer?: ReturnType<typeof setTimeout>;
  private toastHideTimer?: ReturnType<typeof setTimeout>;
  private toastFadeTimer?: ReturnType<typeof setTimeout>;

  readonly beneficioForm = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100), nonEmptyTrimmed]],
    descricao: [''],
    valor: [0, [Validators.required, Validators.min(0)]],
    ativo: [true],
  });

  readonly transferenciaForm = this.fb.nonNullable.group({
    origemId: [0, [Validators.required, Validators.min(1)]],
    destinoId: [0, [Validators.required, Validators.min(1)]],
    valor: [0, [Validators.required, Validators.min(0.01)]],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly beneficioService: BeneficioService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchBeneficios();
  }

  ngOnDestroy(): void {
    this.clearListSafetyTimer();
    this.clearToastTimers();
    this.listSub?.unsubscribe();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.confirmDeleteBeneficio) {
      this.cancelDeleteConfirm();
      return;
    }
    if (this.cadastroModalOpen) {
      this.closeCadastroModal();
      return;
    }
    if (this.transferModalOpen) {
      this.closeTransferModal();
    }
  }

  get filteredBeneficios(): Beneficio[] {
    const term = this.searchTerm.trim().toLowerCase();

    return this.beneficios.filter((beneficio) => {
      const matchesSearch =
        !term ||
        beneficio.nome.toLowerCase().includes(term) ||
        (beneficio.descricao ?? '').toLowerCase().includes(term);

      const matchesStatus =
        this.statusFilter === 'all' ||
        (this.statusFilter === 'active' && beneficio.ativo) ||
        (this.statusFilter === 'inactive' && !beneficio.ativo);

      return matchesSearch && matchesStatus;
    });
  }

  get totalAtivos(): number {
    return this.beneficios.filter((b) => b.ativo).length;
  }

  get saldoTotal(): number {
    return this.beneficios.reduce((sum, b) => sum + b.valor, 0);
  }

  get isEditing(): boolean {
    return this.selectedId !== null;
  }

  get hasBeneficios(): boolean {
    return this.beneficios.length > 0;
  }

  get hasOpenModal(): boolean {
    return this.cadastroModalOpen || this.transferModalOpen || this.confirmDeleteBeneficio !== null;
  }

  get tableRows(): Beneficio[] {
    return this.filteredBeneficios;
  }

  get canSubmitCadastro(): boolean {
    return this.beneficioForm.valid && !this.saving;
  }

  get canSubmitTransferencia(): boolean {
    const origemId = Number(this.transferenciaForm.controls.origemId.value);
    const destinoId = Number(this.transferenciaForm.controls.destinoId.value);
    const valor = Number(this.transferenciaForm.controls.valor.value);

    return (
      this.beneficios.length >= 2 &&
      origemId >= 1 &&
      destinoId >= 1 &&
      origemId !== destinoId &&
      valor >= 0.01 &&
      !this.transferring
    );
  }

  get transferSameAccountSelected(): boolean {
    const origemId = Number(this.transferenciaForm.controls.origemId.value);
    const destinoId = Number(this.transferenciaForm.controls.destinoId.value);
    return origemId >= 1 && destinoId >= 1 && origemId === destinoId;
  }

  loadBeneficios(): void {
    this.fetchBeneficios({ refresh: true, successMessage: 'Lista atualizada com sucesso!' });
  }

  openCreate(): void {
    this.selectedId = null;
    this.highlightedId = null;
    this.beneficioForm.reset({
      nome: '',
      descricao: '',
      valor: 0,
      ativo: true,
    });
    this.beneficioValorDisplay = formatBrl(0);
    this.beneficioForm.markAsUntouched();
    this.beneficioForm.markAsPristine();
    this.cadastroModalOpen = true;
  }

  edit(beneficio: Beneficio): void {
    this.selectedId = beneficio.id;
    this.highlightedId = beneficio.id;
    this.beneficioForm.patchValue({
      nome: beneficio.nome,
      descricao: beneficio.descricao ?? '',
      valor: beneficio.valor,
      ativo: beneficio.ativo,
    });
    this.beneficioValorDisplay = formatBrl(beneficio.valor);
    this.beneficioForm.markAsUntouched();
    this.cadastroModalOpen = true;
  }

  openTransfer(beneficio?: Beneficio): void {
    this.transferenciaForm.reset({
      origemId: beneficio?.id ?? 0,
      destinoId: 0,
      valor: 0,
    });
    this.transferenciaValorDisplay = formatBrl(0);
    this.transferenciaForm.markAsUntouched();
    this.transferenciaForm.markAsPristine();
    this.transferModalOpen = true;
  }

  closeCadastroModal(force = false): void {
    if (this.saving && !force) {
      return;
    }
    this.cadastroModalOpen = false;
    this.selectedId = null;
    this.highlightedId = null;
    this.beneficioForm.reset({
      nome: '',
      descricao: '',
      valor: 0,
      ativo: true,
    });
    this.beneficioValorDisplay = formatBrl(0);
  }

  closeTransferModal(force = false): void {
    if (this.transferring && !force) {
      return;
    }
    this.transferModalOpen = false;
    this.transferenciaForm.reset({
      origemId: 0,
      destinoId: 0,
      valor: 0,
    });
    this.transferenciaValorDisplay = formatBrl(0);
  }

  onBeneficioValorInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = parseBrlInput(input.value);
    this.beneficioValorDisplay = maskBrlFromInput(input.value);
    input.value = this.beneficioValorDisplay;
    this.beneficioForm.controls.valor.setValue(value);
    this.beneficioForm.controls.valor.markAsDirty();
  }

  onTransferenciaValorInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = parseBrlInput(input.value);
    this.transferenciaValorDisplay = maskBrlFromInput(input.value);
    input.value = this.transferenciaValorDisplay;
    this.transferenciaForm.controls.valor.setValue(value);
    this.transferenciaForm.controls.valor.markAsDirty();
  }

  saveBeneficio(): void {
    if (this.beneficioForm.invalid) {
      this.beneficioForm.markAllAsTouched();
      return;
    }

    const payload: BeneficioPayload = {
      nome: this.beneficioForm.controls.nome.value.trim(),
      descricao: this.beneficioForm.controls.descricao.value.trim() || null,
      valor: Number(this.beneficioForm.controls.valor.value),
      ativo: this.beneficioForm.controls.ativo.value,
    };

    this.saving = true;
    this.clearFeedback();

    const request$ = this.selectedId
      ? this.beneficioService.update(this.selectedId, payload)
      : this.beneficioService.create(payload);

    const wasEdit = this.selectedId !== null;

    request$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        const nome = payload.nome;
        const successMessage = wasEdit
          ? `Benefício "${nome}" atualizado com sucesso!`
          : `Benefício "${nome}" cadastrado com sucesso!`;
        this.closeCadastroModal(true);
        this.fetchBeneficios({ refresh: true, successMessage });
      },
      error: (error) => this.handleError(error),
    });
  }

  requestDelete(beneficio: Beneficio): void {
    this.confirmDeleteBeneficio = beneficio;
  }

  cancelDeleteConfirm(): void {
    if (this.deletingId !== null) {
      return;
    }
    this.confirmDeleteBeneficio = null;
  }

  confirmDelete(): void {
    const beneficio = this.confirmDeleteBeneficio;
    if (!beneficio) {
      return;
    }

    this.deletingId = beneficio.id;
    this.clearFeedback();

    this.beneficioService
      .delete(beneficio.id)
      .pipe(finalize(() => (this.deletingId = null)))
      .subscribe({
        next: () => {
          const successMessage = `Benefício "${beneficio.nome}" removido com sucesso!`;
          this.confirmDeleteBeneficio = null;
          if (this.selectedId === beneficio.id) {
            this.closeCadastroModal(true);
          }
          this.fetchBeneficios({ refresh: true, successMessage });
        },
        error: (error) => this.handleError(error),
      });
  }

  transferir(): void {
    if (this.transferenciaForm.invalid) {
      this.transferenciaForm.markAllAsTouched();
      return;
    }

    const origemId = Number(this.transferenciaForm.controls.origemId.value);
    const destinoId = Number(this.transferenciaForm.controls.destinoId.value);

    if (origemId === destinoId) {
      return;
    }

    const payload: TransferenciaPayload = {
      origemId,
      destinoId,
      valor: Number(this.transferenciaForm.controls.valor.value),
    };

    this.transferring = true;
    this.clearFeedback();

    this.beneficioService
      .transfer(payload)
      .pipe(finalize(() => (this.transferring = false)))
      .subscribe({
        next: (resultado) => {
          const successMessage = `Transferência concluída! Origem: ${this.formatCurrency(
            resultado.saldoOrigem
          )} · Destino: ${this.formatCurrency(resultado.saldoDestino)}`;
          this.closeTransferModal(true);
          this.fetchBeneficios({ refresh: true, successMessage });
        },
        error: (error) => this.handleError(error),
      });
  }

  clearSearch(): void {
    this.searchTerm = '';
  }

  trackById(_index: number, beneficio: Beneficio): number {
    return beneficio.id;
  }

  formatCurrency(value: number): string {
    return formatBrl(value);
  }

  isRowBusy(beneficioId: number): boolean {
    return this.deletingId === beneficioId;
  }

  isBeneficioFieldInvalid(field: 'nome'): boolean {
    const control = this.beneficioForm.controls[field];
    return control.invalid && (control.touched || control.dirty);
  }

  isTransferFieldInvalid(field: 'origemId' | 'destinoId' | 'valor'): boolean {
    const control = this.transferenciaForm.controls[field];
    return control.invalid && (control.touched || control.dirty);
  }

  markBeneficioValorTouched(): void {
    this.beneficioForm.controls.valor.markAsTouched();
  }

  markTransferenciaValorTouched(): void {
    this.transferenciaForm.controls.valor.markAsTouched();
  }

  private fetchBeneficios(options: { refresh?: boolean; successMessage?: string } = {}): void {
    const isRefresh = options.refresh ?? false;
    const successMessage = options.successMessage;

    this.listSub?.unsubscribe();
    this.clearListSafetyTimer();

    const isRefreshRequest = isRefresh && this.hasBeneficios;
    if (isRefreshRequest) {
      this.refreshingList = true;
    } else {
      this.initialLoading = true;
    }

    if (!isRefresh) {
      this.clearFeedback();
    } else if (successMessage) {
      this.error = '';
    } else {
      this.clearFeedback();
    }

    this.listSafetyTimer = setTimeout(() => {
      if (this.initialLoading || this.refreshingList) {
        this.listLoadFailed = true;
        this.error =
          'Tempo esgotado ao conectar ao servidor. Verifique se a aplicação está em execução.';
        this.endListRequest();
      }
    }, LIST_SAFETY_MS);

    this.listSub = this.beneficioService
      .list()
      .pipe(
        take(1),
        finalize(() => this.endListRequest())
      )
      .subscribe({
        next: (beneficios) => {
          this.listLoadFailed = false;
          this.beneficios = beneficios ?? [];
          if (successMessage) {
            this.showToast(successMessage, 'success');
          } else if (isRefreshRequest) {
            this.showToast('Lista atualizada com sucesso!', 'success');
          }
        },
        error: (error) => {
          this.listLoadFailed = true;
          this.handleError(error);
        },
      });
  }

  private endListRequest(): void {
    this.clearListSafetyTimer();
    this.stopListLoading();
    this.cdr.markForCheck();
  }

  private clearListSafetyTimer(): void {
    if (this.listSafetyTimer) {
      clearTimeout(this.listSafetyTimer);
      this.listSafetyTimer = undefined;
    }
  }

  private stopListLoading(): void {
    this.initialLoading = false;
    this.refreshingList = false;
  }

  private clearFeedback(): void {
    this.error = '';
    this.listLoadFailed = false;
  }

  private showToast(text: string, type: ToastType = 'success'): void {
    this.clearToastTimers();
    this.toastLeaving = false;
    this.toast = { text, type };
    this.cdr.markForCheck();

    this.toastFadeTimer = setTimeout(() => {
      this.toastLeaving = true;
      this.cdr.markForCheck();
    }, TOAST_DURATION_MS - TOAST_FADE_MS);

    this.toastHideTimer = setTimeout(() => {
      this.toast = null;
      this.toastLeaving = false;
      this.cdr.markForCheck();
    }, TOAST_DURATION_MS);
  }

  private clearToastTimers(): void {
    if (this.toastFadeTimer) {
      clearTimeout(this.toastFadeTimer);
      this.toastFadeTimer = undefined;
    }
    if (this.toastHideTimer) {
      clearTimeout(this.toastHideTimer);
      this.toastHideTimer = undefined;
    }
  }

  private handleError(error: unknown): void {
    const apiError = error as {
      status?: number;
      error?: { messages?: string[]; message?: string };
      message?: string;
    };

    const text =
      apiError.status === 0
        ? 'Servidor indisponível. Verifique se a aplicação está em execução.'
        : (apiError.error?.messages?.join(' ') ??
          apiError.error?.message ??
          apiError.message ??
          'Não foi possível concluir a operação.');

    if (this.listLoadFailed || this.initialLoading) {
      this.error = text;
      return;
    }

    this.showToast(text, 'error');
  }
}
