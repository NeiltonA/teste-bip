import { formatBrl, maskBrlFromInput, parseBrlInput } from './currency.util';

describe('currency.util', () => {
  it('should format BRL', () => {
    expect(formatBrl(1234.56)).toContain('1.234,56');
  });

  it('should parse typed digits as centavos', () => {
    expect(parseBrlInput('R$ 1.234,56')).toBe(1234.56);
    expect(parseBrlInput('123456')).toBe(1234.56);
  });

  it('should mask while typing', () => {
    expect(maskBrlFromInput('150')).toContain('1,50');
  });
});
