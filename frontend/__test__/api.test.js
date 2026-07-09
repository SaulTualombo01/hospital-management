// Simulamos el objeto global fetch
global.fetch = jest.fn();

// Importamos el módulo (asegúrate de que el bloque de exportación al final de api.js esté correcto)
const { PacientesAPI, DoctoresAPI, CitasAPI, HistoriasAPI } = require('../js/api');

describe('Pruebas Unitarias API con Mock de Fetch - api.js', () => {

    beforeEach(() => {
        // Limpiamos el mock antes de cada prueba para que no se mezclen los llamados
        fetch.mockClear();
    });

    // ==========================================
    // PRUEBAS DE PACIENTES API (GET y POST)
    // ==========================================
    describe('PacientesAPI', () => {
        test('listar() - Debe realizar un GET correctamente y parsear el JSON', async () => {
            const mockPacientes = [{ id: 1, nombre: 'Juan' }, { id: 2, nombre: 'Ana' }];
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockPacientes
            });

            const result = await PacientesAPI.listar();

            expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/pacientes', expect.any(Object));
            expect(result).toEqual(mockPacientes);
        });

        test('crear() - Debe enviar un POST con el body correcto', async () => {
            const nuevoPaciente = { nombre: 'Carlos', apellido: 'Prueba' };
            const respuestaEsperada = { id: 3, ...nuevoPaciente };

            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => respuestaEsperada
            });

            const result = await PacientesAPI.crear(nuevoPaciente);

            // Verificamos que se haya llamado con el método POST y el body en JSON
            expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/pacientes', expect.objectContaining({
                method: 'POST',
                body: JSON.stringify(nuevoPaciente)
            }));
            expect(result).toEqual(respuestaEsperada);
        });
    });

    // ==========================================
    // PRUEBAS DE DOCTORES API (DELETE y Bugs)
    // ==========================================
    describe('DoctoresAPI', () => {
        test('eliminar() - Bug Intencional: Falla al parsear respuesta vacía (204 No Content)', async () => {
            // El backend al eliminar suele devolver 204 No Content con un body vacío.
            // El bug en apiFetch hace un "await response.json()" sin importar el método.
            fetch.mockResolvedValueOnce({
                ok: true,
                status: 204,
                json: async () => { throw new SyntaxError("Unexpected end of JSON input"); }
            });

            // Demostramos que llamar a eliminar rompe la ejecución porque intenta parsear JSON inexistente
            await expect(DoctoresAPI.eliminar(1)).rejects.toThrow(SyntaxError);
        });
    });

    // ==========================================
    // PRUEBAS DE CITAS API (Búsqueda por ID)
    // ==========================================
    describe('CitasAPI', () => {
        test('buscar() - Debe concatenar correctamente el ID en la URL', async () => {
            const mockCita = { id: 15, pacienteId: 1, doctorId: 2 };
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockCita
            });

            await CitasAPI.buscar(15);

            expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/citas/15', expect.any(Object));
        });
    });

    // ==========================================
    // PRUEBAS DE MANEJO DE ERRORES (BUGS BASE)
    // ==========================================
    describe('Manejo de Errores Globales (apiFetch)', () => {
        test('Bug Intencional: Retorna la data sin lanzar excepción si ok es false', async () => {
            const errorBackend = { error: 'Not Found', message: 'Paciente no existe' };

            fetch.mockResolvedValueOnce({
                ok: false,
                status: 404,
                json: async () => errorBackend
            });

            const result = await PacientesAPI.buscar(999);

            // La función idealmente debería hacer "if (!response.ok) throw new Error(...)"
            // Demostramos la deuda técnica: no lanza error, solo retorna el JSON del error silenciosamente
            expect(result).toEqual(errorBackend);
        });

        test('Manejo de Promesa Rechazada (Network Error)', async () => {
            // Simulamos que el servidor está apagado (falla de red)
            fetch.mockRejectedValueOnce(new Error('Failed to fetch'));

            // Como apiFetch no tiene un bloque try/catch para manejar caídas de red,
            // la excepción debe propagarse hacia arriba.
            await expect(PacientesAPI.listar()).rejects.toThrow('Failed to fetch');
        });
    });
    // ==========================================
    // PRUEBAS DE HISTORIAS CLÍNICAS API
    // ==========================================
    describe('HistoriasAPI', () => {
        test('porPaciente() - Debe construir la URL correctamente para búsquedas anidadas', async () => {
            const mockHistorias = [
                { id: 101, pacienteId: 5, diagnostico: 'Gripe estacional' }
            ];

            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockHistorias
            });

            // Act: Buscamos el historial del paciente con ID 5
            const result = await HistoriasAPI.porPaciente(5);

            // Assert: Verificamos que la URL contenga la ruta anidada correcta
            expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/historias-clinicas/paciente/5', expect.any(Object));
            expect(result).toEqual(mockHistorias);
        });

        test('crear() - Debe enviar el payload completo incluyendo diagnóstico', async () => {
            const nuevaHistoria = {
                pacienteId: 5,
                doctorId: 2,
                diagnostico: 'Revisión general',
                tratamiento: 'Paracetamol'
            };

            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => ({ id: 102, ...nuevaHistoria })
            });

            await HistoriasAPI.crear(nuevaHistoria);

            expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/historias-clinicas', expect.objectContaining({
                method: 'POST',
                body: JSON.stringify(nuevaHistoria)
            }));
        });
    });
});