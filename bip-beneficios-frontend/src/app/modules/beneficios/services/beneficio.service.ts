import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, TimeoutError, throwError, timeout } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  Beneficio,
  BeneficioPayload,
  TransferenciaPayload,
  TransferenciaResultado,
} from '../models/beneficio.model';

const LIST_TIMEOUT_MS = 10_000;

@Injectable({ providedIn: 'root' })
export class BeneficioService {
  private readonly apiUrl = '/api/v1/beneficios';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<Beneficio[]> {
    return this.http.get<Beneficio[]>(this.apiUrl).pipe(
      timeout({ first: LIST_TIMEOUT_MS }),
      catchError((error) => {
        if (error instanceof TimeoutError) {
          return throwError(() => ({
            status: 0,
            error: {
              message:
                'Tempo esgotado ao buscar benefícios. Verifique se o servidor está em http://localhost:8080',
            },
          }));
        }
        return throwError(() => error);
      })
    );
  }

  create(payload: BeneficioPayload): Observable<Beneficio> {
    return this.http.post<Beneficio>(this.apiUrl, payload);
  }

  update(id: number, payload: BeneficioPayload): Observable<Beneficio> {
    return this.http.put<Beneficio>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  transfer(payload: TransferenciaPayload): Observable<TransferenciaResultado> {
    return this.http.post<TransferenciaResultado>(`${this.apiUrl}/transferencias`, payload);
  }
}
