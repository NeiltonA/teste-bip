import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { Beneficio } from '../../models/beneficio.model';
import { BeneficioService } from '../../services/beneficio.service';
import { BeneficioPageComponent } from './beneficio-page.component';

const seedBeneficio = (id: number, nome: string, valor: number): Beneficio => ({
  id,
  nome,
  descricao: null,
  valor,
  ativo: true,
  version: 0,
});

describe('BeneficioPageComponent', () => {
  let service: jasmine.SpyObj<BeneficioService>;

  beforeEach(() => {
    service = jasmine.createSpyObj<BeneficioService>('BeneficioService', [
      'list',
      'create',
      'update',
      'delete',
      'transfer',
    ]);
    service.list.and.returnValue(of([]));
  });

  it('should load beneficios on init', () => {
    const cdr = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck']);
    const component = new BeneficioPageComponent(new FormBuilder(), service, cdr);

    component.ngOnInit();

    expect(service.list).toHaveBeenCalled();
    expect(component.beneficios).toEqual([]);
  });

  it('should format currency in pt-BR', () => {
    const cdr = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck']);
    const component = new BeneficioPageComponent(new FormBuilder(), service, cdr);

    expect(component.formatCurrency(10)).toContain('10,00');
  });

  it('should disable cadastro submit until nome is filled', () => {
    const cdr = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck']);
    const component = new BeneficioPageComponent(new FormBuilder(), service, cdr);

    expect(component.canSubmitCadastro).toBeFalse();

    component.beneficioForm.controls.nome.setValue('Vale refeicao');
    expect(component.canSubmitCadastro).toBeTrue();
  });

  it('should disable transfer submit until required fields are valid', () => {
    const cdr = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck']);
    const component = new BeneficioPageComponent(new FormBuilder(), service, cdr);
    component.beneficios = [
      { id: 1, nome: 'A', descricao: null, valor: 100, ativo: true, version: 0 },
      { id: 2, nome: 'B', descricao: null, valor: 200, ativo: true, version: 0 },
    ];

    expect(component.canSubmitTransferencia).toBeFalse();

    component.transferenciaForm.patchValue({ origemId: 1, destinoId: 2, valor: 10 });
    expect(component.canSubmitTransferencia).toBeTrue();
  });

  describe('fluxos HTTP', () => {
    let fixture: ComponentFixture<BeneficioPageComponent>;
    let component: BeneficioPageComponent;
    let httpMock: HttpTestingController;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [BeneficioPageComponent, HttpClientTestingModule],
      });
      fixture = TestBed.createComponent(BeneficioPageComponent);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      fixture.detectChanges();
      httpMock.expectOne('/api/v1/beneficios').flush([]);
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('deve cadastrar benefício, fechar modal e exibir toast de sucesso', () => {
      component.openCreate();
      component.beneficioForm.patchValue({ nome: 'Vale refeição', valor: 150, ativo: true });

      component.saveBeneficio();

      const createReq = httpMock.expectOne('/api/v1/beneficios');
      expect(createReq.request.method).toBe('POST');
      expect(createReq.request.body.nome).toBe('Vale refeição');
      createReq.flush(seedBeneficio(3, 'Vale refeição', 150));

      const listReq = httpMock.expectOne('/api/v1/beneficios');
      listReq.flush([seedBeneficio(3, 'Vale refeição', 150)]);

      expect(component.cadastroModalOpen).toBeFalse();
      expect(component.toast?.type).toBe('success');
      expect(component.toast?.text).toContain('cadastrado com sucesso');
    });

    it('deve atualizar benefício existente', () => {
      component.beneficios = [seedBeneficio(1, 'Benefício A', 1000)];
      component.edit(component.beneficios[0]);
      component.beneficioForm.patchValue({ nome: 'Benefício A Plus', valor: 1100 });

      component.saveBeneficio();

      const updateReq = httpMock.expectOne('/api/v1/beneficios/1');
      expect(updateReq.request.method).toBe('PUT');
      updateReq.flush(seedBeneficio(1, 'Benefício A Plus', 1100));

      httpMock.expectOne('/api/v1/beneficios').flush([seedBeneficio(1, 'Benefício A Plus', 1100)]);

      expect(component.cadastroModalOpen).toBeFalse();
      expect(component.toast?.text).toContain('atualizado com sucesso');
    });

    it('deve concluir transferência e fechar modal', () => {
      component.beneficios = [seedBeneficio(1, 'A', 100), seedBeneficio(2, 'B', 500)];
      component.transferenciaForm.patchValue({ origemId: 1, destinoId: 2, valor: 25 });
      component.transferModalOpen = true;

      component.transferir();

      const transferReq = httpMock.expectOne('/api/v1/beneficios/transferencias');
      expect(transferReq.request.body).toEqual({ origemId: 1, destinoId: 2, valor: 25 });
      transferReq.flush({
        origemId: 1,
        destinoId: 2,
        valorTransferido: 25,
        saldoOrigem: 75,
        saldoDestino: 525,
      });

      httpMock.expectOne('/api/v1/beneficios').flush(component.beneficios);

      expect(component.transferModalOpen).toBeFalse();
      expect(component.toast?.type).toBe('success');
      expect(component.toast?.text).toContain('Transferência concluída');
    });

    it('deve exibir toast de erro quando transferência falha', () => {
      component.beneficios = [seedBeneficio(1, 'A', 100), seedBeneficio(2, 'B', 500)];
      component.transferenciaForm.patchValue({ origemId: 1, destinoId: 2, valor: 999 });
      component.transferModalOpen = true;

      component.transferir();

      const transferReq = httpMock.expectOne('/api/v1/beneficios/transferencias');
      transferReq.flush(
        { message: 'Saldo insuficiente no benefício 1' },
        { status: 422, statusText: 'Unprocessable Entity' }
      );

      expect(component.transferModalOpen).toBeTrue();
      expect(component.toast?.type).toBe('error');
      expect(component.toast?.text).toContain('Saldo insuficiente');
    });
  });

  describe('fluxos com service mockado', () => {
    let service: jasmine.SpyObj<BeneficioService>;
    let component: BeneficioPageComponent;

    beforeEach(() => {
      service = jasmine.createSpyObj<BeneficioService>('BeneficioService', [
        'list',
        'create',
        'update',
        'delete',
        'transfer',
      ]);
      service.list.and.returnValue(of([]));
      const cdr = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck']);
      component = new BeneficioPageComponent(new FormBuilder(), service, cdr);
    });

    it('deve propagar erro de create para toast', () => {
      service.create.and.returnValue(
        throwError(() => ({
          status: 400,
          error: { messages: ['nome: Nome é obrigatório'] },
        }))
      );
      component.openCreate();
      component.beneficioForm.patchValue({ nome: 'X', valor: 10, ativo: true });

      component.saveBeneficio();

      expect(component.cadastroModalOpen).toBeTrue();
      expect(component.toast?.type).toBe('error');
      expect(component.toast?.text).toContain('Nome é obrigatório');
    });
  });
});
