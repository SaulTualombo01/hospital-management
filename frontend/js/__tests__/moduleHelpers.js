/* global , jest */
const {loadScript} = require('./testHelpers');

function loadFrontendModules() {
    const showAlertMock = jest.fn();

    const utils = loadScript('utils.js', [
        'formatDate',
        'formatDateTime',
        'escapeHTML',
        'showAlert',
        'validateEmail',
        'validateTelefono',
        'isFutureDate',
        'localToISO',
    ]);

    const api = loadScript('api.js', [
        'apiFetch',
        'PacientesAPI',
        'DoctoresAPI',
        'CitasAPI',
        'HistoriasAPI',
    ]);

    const pacientes = loadScript('pacientes.js', ['PacientesModule'], {
        formatDate: utils.formatDate,
        PacientesAPI: api.PacientesAPI,
        showAlert: showAlertMock,
    });

    const doctores = loadScript('doctores.js', ['DoctoresModule'], {
        DoctoresAPI: api.DoctoresAPI,
        showAlert: showAlertMock,
    });

    const citas = loadScript('citas.js', ['CitasModule'], {
        formatDateTime: utils.formatDateTime,
        localToISO: utils.localToISO,
        CitasAPI: api.CitasAPI,
        DoctoresAPI: api.DoctoresAPI,
        PacientesAPI: api.PacientesAPI,
        showAlert: showAlertMock,
    });

    const historias = loadScript('historias.js', ['HistoriasModule'], {
        formatDateTime: utils.formatDateTime,
        HistoriasAPI: api.HistoriasAPI,
        DoctoresAPI: api.DoctoresAPI,
        PacientesAPI: api.PacientesAPI,
        showAlert: showAlertMock,
    });

    return {
        utils,
        api,
        PacientesModule: pacientes.PacientesModule,
        DoctoresModule: doctores.DoctoresModule,
        CitasModule: citas.CitasModule,
        HistoriasModule: historias.HistoriasModule,
        showAlertMock,
    };
}

module.exports = {loadFrontendModules};