import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { NegocioStore } from '../core/negocio.store';
import { MoneyPipe } from './money.pipe';

describe('MoneyPipe', () => {
  it('formatea en bolivianos con dos decimales', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), MoneyPipe] });
    const pipe = TestBed.inject(MoneyPipe);
    expect(pipe.transform(1234.5)).toBe('Bs 1.234,50');
    expect(pipe.transform(null)).toBe('—');
  });

  it('usa el símbolo configurado del negocio', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), MoneyPipe] });
    TestBed.inject(NegocioStore).negocio.set({
      name: 'X',
      taxId: null,
      address: null,
      phone: null,
      currency: 'USD',
      currencySymbol: '$',
      taxRate: 0,
      receiptMessage: null,
    });
    expect(TestBed.inject(MoneyPipe).transform(10)).toBe('$ 10,00');
  });
});
