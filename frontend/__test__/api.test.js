// Simulamos el objeto global fetch
global.fetch = jest.fn();

// Importamos el módulo (asegúrate de exportar PacientesAPI al final de api.js)
const { PacientesAPI } = require('../js/api');

describe('Pruebas Unitarias API con Mock de Fetch - api.js', () => {

    beforeEach(() => {
        fetch.mockClear();
    });

    test('Debe listar pacientes correctamente parseando el JSON', async () => {
        // Arrange: Preparamos la respuesta falsa de la API
        const mockPacientes = [{ id: 1, nombre: 'Juan' }];
        fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockPacientes
        });

        // Act: Llamamos a la función
        const result = await PacientesAPI.listar();

        // Assert: Verificamos que fetch se llamó con la URL correcta y retornó la data
        expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/pacientes', expect.any(Object));
        expect(result).toEqual(mockPacientes);
    });

    test('Manejo de Errores (Bug Intencional): Retorna data sin lanzar excepción si ok es false', async () => {
        fetch.mockResolvedValueOnce({
            ok: false,
            status: 500,
            json: async () => ({ error: 'Error interno' })
        });

        const result = await PacientesAPI.listar();

        // Demuestra la deuda técnica: no lanza error, solo retorna el JSON del error
        expect(result).toHaveProperty('error', 'Error interno');
    });
});