import { describe, expect, it, vi } from 'vitest';
import { ApiError, onUnauthorized, request, setToken } from '../api/client';

function mockFetch(status, body) {
  return vi.spyOn(globalThis, 'fetch').mockResolvedValue(
    new Response(body === undefined ? null : JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  );
}

describe('api client', () => {
  it('sends the bearer token and query params, and parses JSON', async () => {
    setToken('abc');
    const fetchSpy = mockFetch(200, [{ uuid: '1' }]);

    const data = await request('/business', { params: { q: 'sol', page: 0, empty: '' } });

    expect(data).toEqual([{ uuid: '1' }]);
    const [url, options] = fetchSpy.mock.calls[0];
    expect(url).toBe('/api/business?q=sol&page=0');
    expect(options.headers.Authorization).toBe('Bearer abc');
  });

  it('turns backend errors into ApiError with field errors', async () => {
    mockFetch(400, { status: 400, error: 'Validation error', details: ['cif: CIF format is not valid', 'name: Name must not be blank'] });

    const error = await request('/business', { method: 'POST', body: {} }).catch((e) => e);

    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(400);
    expect(error.fieldErrors).toEqual({ cif: 'CIF format is not valid', name: 'Name must not be blank' });
  });

  it('returns null for 204 responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 204 }));
    expect(await request('/business/1', { method: 'DELETE' })).toBeNull();
  });

  it('calls the unauthorized handler only when a token was sent', async () => {
    const handler = vi.fn();
    onUnauthorized(handler);

    mockFetch(401, { error: 'Invalid credentials', details: ['Email or password is incorrect'] });
    await request('/auth/login', { method: 'POST', body: {}, auth: false }).catch(() => {});
    expect(handler).not.toHaveBeenCalled();

    setToken('expired');
    mockFetch(401, { error: 'Unauthorized', details: [] });
    await request('/booking/mine').catch(() => {});
    expect(handler).toHaveBeenCalledTimes(1);
    onUnauthorized(null);
  });

  it('reports network failures with a readable message', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));
    const error = await request('/business').catch((e) => e);
    expect(error.status).toBe(0);
    expect(error.message).toMatch(/servidor/);
  });
});
