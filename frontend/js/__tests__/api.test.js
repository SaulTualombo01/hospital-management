/* global describe, beforeEach, test, expect, jest */
const {loadScript} = require('./testHelpers');

describe('api.js', () => {
    let fetchMock;
    let api;

    beforeEach(() => {
        fetchMock = jest.fn();
        api = loadScript('api.js', [
            'apiFetch',
            'PacientesAPI',
            'DoctoresAPI',
            'CitasAPI',
            'HistoriasAPI',
        ], {
            fetch: fetchMock,
        });
    });

    test('apiFetch hace la llamada base y retorna el JSON parseado', async () => {
        fetchMock.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ok: true}),
        });

        const resultado = await api.apiFetch('/ping');

        expect(fetchMock).toHaveBeenCalledWith(
            'http://localhost:8080/api/ping',
            expect.objectContaining({
                headers: {
                    'Content-Type': 'application/json',
                },
            })
        );
        expect(resultado).toEqual({ok: true});
    });

    test('apiFetch conserva headers personalizados cuando no hay conflicto', async () => {
        fetchMock.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ok: true}),
        });

        await api.apiFetch('/ping', {
            method: 'POST',
            body: JSON.stringify({nombre: 'Ana'}),
            headers: {
                Authorization: 'Bearer token',
            },
        });

        expect(fetchMock).toHaveBeenCalledWith(
            'http://localhost:8080/api/ping',
            expect.objectContaining({
                method: 'POST',
                body: JSON.stringify({nombre: 'Ana'}),
                headers: {
                    Authorization: 'Bearer token',
                },
            })
        );
    });

    test.failing('apiFetch deberia mantener Content-Type cuando se pasan headers personalizados', async () => {
        fetchMock.mockResolvedValueOnce({
            ok: true,
            json: async () => ({ok: true}),
        });

        await api.apiFetch('/ping', {
            method: 'POST',
            body: JSON.stringify({nombre: 'Ana'}),
            headers: {
                Authorization: 'Bearer token',
            },
        });

        expect(fetchMock).toHaveBeenCalledWith(
            'http://localhost:8080/api/ping',
            expect.objectContaining({
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer token',
                },
            })
        );
    });

    test('PacientesAPI arma correctamente los endpoints principales', async () => {
        fetchMock
            .mockResolvedValueOnce({ok: true, json: async () => [{id: 1}]})
            .mockResolvedValueOnce({ok: true, json: async () => ({id: 2})})
            .mockResolvedValueOnce({ok: true, json: async () => ({id: 3})})
            .mockResolvedValueOnce({ok: true, json: async () => ({id: 4})})
            .mockResolvedValueOnce({ok: true, json: async () => ({deleted: true})})
            .mockResolvedValueOnce({ok: true, json: async () => [{id: 5}]})
            .mockResolvedValueOnce({ok: true, json: async () => 27.4});

        await api.PacientesAPI.listar();
        await api.PacientesAPI.buscar(2);
        await api.PacientesAPI.crear({nombre: 'Ana'});
        await api.PacientesAPI.actualizar(3, {nombre: 'Maria'});
        await api.PacientesAPI.eliminar(4);
        await api.PacientesAPI.buscarPorNombre('Ana');
        await api.PacientesAPI.edadPromedio();

        expect(fetchMock).toHaveBeenNthCalledWith(
            1,
            'http://localhost:8080/api/pacientes',
            expect.objectContaining({
                headers: {
                    'Content-Type': 'application/json',
                },
            })
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            2,
            'http://localhost:8080/api/pacientes/2',
            expect.any(Object)
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            3,
            'http://localhost:8080/api/pacientes',
            expect.objectContaining({method: 'POST'})
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            4,
            'http://localhost:8080/api/pacientes/3',
            expect.objectContaining({method: 'PUT'})
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            5,
            'http://localhost:8080/api/pacientes/4',
            expect.objectContaining({method: 'DELETE'})
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            6,
            'http://localhost:8080/api/pacientes/buscar?nombre=Ana',
            expect.any(Object)
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            7,
            'http://localhost:8080/api/pacientes/estadisticas/edad-promedio',
            expect.any(Object)
        );
    });

    test('DoctoresAPI arma correctamente los endpoints de busqueda', async () => {
        fetchMock
            .mockResolvedValueOnce({ok: true, json: async () => []})
            .mockResolvedValueOnce({ok: true, json: async () => []});

        await api.DoctoresAPI.buscarPorEspecialidad('Cardiología');
        await api.DoctoresAPI.buscarPorNombre('Ana', 'Perez');

        expect(fetchMock).toHaveBeenNthCalledWith(
            1,
            'http://localhost:8080/api/doctores/buscar-especialidad?q=Cardiolog%C3%ADa',
            expect.any(Object)
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            2,
            'http://localhost:8080/api/doctores/buscar-nombre?nombre=Ana&apellido=Perez',
            expect.any(Object)
        );
    });

    test('CitasAPI arma correctamente el filtro por rango de fechas', async () => {
        fetchMock.mockResolvedValueOnce({ok: true, json: async () => []});

        await api.CitasAPI.porRangoFechas('2026-07-08T00:00:00', '2026-07-09T00:00:00');

        expect(fetchMock).toHaveBeenCalledWith(
            'http://localhost:8080/api/citas/rango-fechas?inicio=2026-07-08T00:00:00&fin=2026-07-09T00:00:00',
            expect.any(Object)
        );
    });

    test('HistoriasAPI arma correctamente el filtro por paciente y doctor', async () => {
        fetchMock
            .mockResolvedValueOnce({ok: true, json: async () => []})
            .mockResolvedValueOnce({ok: true, json: async () => []});

        await api.HistoriasAPI.porPaciente(10);
        await api.HistoriasAPI.porDoctor(20);

        expect(fetchMock).toHaveBeenNthCalledWith(
            1,
            'http://localhost:8080/api/historias-clinicas/paciente/10',
            expect.any(Object)
        );
        expect(fetchMock).toHaveBeenNthCalledWith(
            2,
            'http://localhost:8080/api/historias-clinicas/doctor/20',
            expect.any(Object)
        );
    });

    test.failing('apiFetch deberia rechazar respuestas no OK', async () => {
        fetchMock.mockResolvedValueOnce({
            ok: false,
            json: async () => ({message: 'Bad Request'}),
        });

        await expect(api.apiFetch('/error')).rejects.toThrow('Bad Request');
    });

    test.failing('apiFetch deberia soportar respuestas DELETE sin cuerpo JSON', async () => {
        fetchMock.mockResolvedValueOnce({
            ok: true,
            json: async () => {
                throw new Error('Unexpected end of JSON input');
            },
        });

        await expect(api.apiFetch('/pacientes/1', {method: 'DELETE'})).resolves.toBeDefined();
    });
});