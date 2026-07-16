/* global describe, beforeEach, test, expect, jest */
const {loadFrontendModules} = require('./moduleHelpers');

describe('PacientesModule', () => {
    let PacientesModule;
    let PacientesAPI;

    beforeEach(() => {
        document.body.innerHTML = `
      <button id="btn-nuevo-paciente"></button>
      <form id="paciente-form">
        <input id="paciente-id" />
        <input id="paciente-nombre" />
        <input id="paciente-apellido" />
        <input id="paciente-email" />
        <input id="paciente-telefono" />
        <input id="paciente-direccion" />
        <input id="paciente-fecha-nacimiento" />
      </form>
      <div id="modal-paciente"></div>
      <input id="search-pacientes" />
      <table id="pacientes-table"><tbody></tbody></table>
      <div id="stat-edad-promedio"></div>
    `;

        const modules = loadFrontendModules();
        PacientesModule = modules.PacientesModule;
        PacientesAPI = modules.api.PacientesAPI;
        global.showAlert = modules.showAlertMock;
    });

    test('renderTabla muestra pacientes en la tabla', () => {
        PacientesModule.renderTabla([
            {
                id: 1,
                nombre: 'Ana',
                apellido: 'Lopez',
                email: 'ana@test.com',
                telefono: '0991234567',
                fechaNacimiento: '1995-01-10T00:00:00Z',
                activo: true,
            },
        ]);

        expect(document.querySelector('#pacientes-table tbody').innerHTML).toContain('Ana Lopez');
        expect(document.querySelector('#pacientes-table tbody').innerHTML).toContain('badge-activo');
    });

    test('renderTabla muestra estado vacio cuando no hay pacientes', () => {
        PacientesModule.renderTabla([]);

        expect(document.querySelector('#pacientes-table tbody').innerHTML).toContain('No hay pacientes registrados');
    });

    test('mostrarFormulario llena el modal con datos del paciente', () => {
        PacientesModule.mostrarFormulario({
            id: 7,
            nombre: 'Maria',
            apellido: 'Gomez',
            email: 'maria@test.com',
            telefono: '0987654321',
            direccion: 'Quito',
            fechaNacimiento: '1990-02-15',
        });

        expect(document.getElementById('paciente-id').value).toBe('7');
        expect(document.getElementById('paciente-nombre').value).toBe('Maria');
        expect(document.getElementById('modal-paciente').classList.contains('show')).toBe(true);
    });

    test('guardarPaciente crea un paciente nuevo', async () => {
        PacientesAPI.crear = jest.fn().mockResolvedValue({id: 1});
        PacientesModule.cerrarFormulario = jest.fn();
        PacientesModule.cargarPacientes = jest.fn().mockResolvedValue();
        PacientesModule.cargarEstadisticas = jest.fn().mockResolvedValue();

        document.getElementById('paciente-id').value = '';
        document.getElementById('paciente-nombre').value = 'Ana';
        document.getElementById('paciente-apellido').value = 'Lopez';
        document.getElementById('paciente-email').value = 'ana@test.com';
        document.getElementById('paciente-telefono').value = '0991234567';
        document.getElementById('paciente-direccion').value = 'Quito';
        document.getElementById('paciente-fecha-nacimiento').value = '1995-01-10';

        await PacientesModule.guardarPaciente({preventDefault: jest.fn()});

        expect(PacientesAPI.crear).toHaveBeenCalledWith(expect.objectContaining({nombre: 'Ana'}));
        expect(PacientesModule.__testShowAlert || global.showAlert).toHaveBeenCalledWith('Paciente creado exitosamente', 'success');
    });

    test('eliminarPaciente invoca la API y recarga la tabla', async () => {
        PacientesAPI.eliminar = jest.fn().mockResolvedValue({});
        PacientesModule.cargarPacientes = jest.fn().mockResolvedValue();

        await PacientesModule.eliminarPaciente(3);

        expect(PacientesAPI.eliminar).toHaveBeenCalledWith(3);
        expect(PacientesModule.__testShowAlert || global.showAlert).toHaveBeenCalledWith('Paciente eliminado exitosamente', 'success');
    });
});