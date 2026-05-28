import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BeneficioService } from './beneficio.service';

describe('BeneficioService', () => {
  let service: BeneficioService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BeneficioService],
    });
    service = TestBed.inject(BeneficioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET beneficios from API', () => {
    service.list().subscribe((items) => expect(items.length).toBe(1));

    const req = httpMock.expectOne('/api/v1/beneficios');
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1, nome: 'A', descricao: null, valor: 100, ativo: true, version: 0 }]);
  });

  it('should POST new beneficio', () => {
    const payload = { nome: 'Vale', descricao: null, valor: 50, ativo: true };

    service.create(payload).subscribe((created) => {
      expect(created.id).toBe(3);
      expect(created.nome).toBe('Vale');
    });

    const req = httpMock.expectOne('/api/v1/beneficios');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 3, nome: 'Vale', descricao: null, valor: 50, ativo: true, version: 0 });
  });

  it('should PUT beneficio by id', () => {
    const payload = { nome: 'Atualizado', descricao: 'x', valor: 200, ativo: false };

    service.update(1, payload).subscribe((updated) => {
      expect(updated.nome).toBe('Atualizado');
    });

    const req = httpMock.expectOne('/api/v1/beneficios/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 1, nome: 'Atualizado', descricao: 'x', valor: 200, ativo: false, version: 1 });
  });

  it('should DELETE beneficio by id', () => {
    let completed = false;
    service.delete(2).subscribe(() => (completed = true));

    const req = httpMock.expectOne('/api/v1/beneficios/2');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBeTrue();
  });

  it('should POST transferencias', () => {
    const payload = { origemId: 1, destinoId: 2, valor: 25 };

    service.transfer(payload).subscribe((result) => {
      expect(result.saldoOrigem).toBe(75);
      expect(result.saldoDestino).toBe(525);
    });

    const req = httpMock.expectOne('/api/v1/beneficios/transferencias');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({
      origemId: 1,
      destinoId: 2,
      valorTransferido: 25,
      saldoOrigem: 75,
      saldoDestino: 525,
    });
  });
});
