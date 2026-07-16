/* global describe, beforeEach, afterEach, test, expect, jest */

const {loadScript} = require('./testHelpers');

describe('utils.js', () => {
    let utils;

    beforeEach(() => {
        document.body.innerHTML = '<div id="alert-container"></div>';
        jest.useRealTimers();
        utils = loadScript('utils.js', [
            'formatDate',
            'formatDateTime',
            'escapeHTML',
            'showAlert',
            'validateEmail',
            'validateTelefono',
            'isFutureDate',
            'localToISO',
        ]);
    });

    afterEach(() => {
        jest.clearAllTimers();
        jest.useRealTimers();
    });

    test('formatDate devuelve una fecha legible en español', () => {
        const resultado = utils.formatDate('2026-07-08T00:00:00Z');

        expect(resultado).toContain('2026');
        expect(resultado.toLowerCase()).toMatch(/jul/i);
    });

    test('formatDateTime devuelve fecha y hora legibles', () => {
        const resultado = utils.formatDateTime('2026-07-08T10:30:00Z');

        expect(resultado).toContain('2026');
        expect(resultado).toMatch(/30/);
    });

    test('escapeHTML escapa caracteres peligrosos basicos', () => {
        const resultado = utils.escapeHTML('5 < 6 & "ok"');

        expect(resultado).toBe('5 &lt; 6 &amp; &quot;ok&quot;');
    });

    test.failing('escapeHTML deberia escapar tambien comillas simples y backticks', () => {
        const resultado = utils.escapeHTML("alert('xss') and `ticks`");

        expect(resultado).toBe('alert(&#39;xss&#39;) and &#96;ticks&#96;');
    });

    test('showAlert inserta una alerta en el contenedor y luego la limpia', () => {
        jest.useFakeTimers();
        utils = loadScript('utils.js', [
            'formatDate',
            'formatDateTime',
            'escapeHTML',
            'showAlert',
            'validateEmail',
            'validateTelefono',
            'isFutureDate',
            'localToISO',
        ]);

        utils.showAlert('Paciente guardado', 'success');

        expect(document.getElementById('alert-container').innerHTML).toContain('alert-success');
        expect(document.getElementById('alert-container').innerHTML).toContain('Paciente guardado');

        jest.advanceTimersByTime(4000);

        expect(document.getElementById('alert-container').innerHTML).toBe('');
    });

    test.failing('showAlert deberia sanitizar el mensaje antes de renderizarlo', () => {
        jest.useFakeTimers();
        utils = loadScript('utils.js', [
            'formatDate',
            'formatDateTime',
            'escapeHTML',
            'showAlert',
            'validateEmail',
            'validateTelefono',
            'isFutureDate',
            'localToISO',
        ]);

        utils.showAlert('<img src=x onerror=alert(1)>', 'error');

        expect(document.getElementById('alert-container').innerHTML).not.toContain('<img');
    });

    test('validateEmail acepta un correo valido con +', () => {
        expect(utils.validateEmail('usuario+tag@hospital.ec')).toBe(true);
    });

    test('validateEmail rechaza un correo sin TLD', () => {
        expect(utils.validateEmail('usuario@hospital')).toBe(false);
    });

    test('validateTelefono acepta exactamente 10 digitos', () => {
        expect(utils.validateTelefono('0991234567')).toBe(true);
    });

    test('validateTelefono rechaza valores no numericos', () => {
        expect(utils.validateTelefono('09912ABCD7')).toBe(false);
    });

    test('isFutureDate detecta fechas futuras y pasadas', () => {
        jest.useFakeTimers();
        jest.setSystemTime(new Date('2026-07-08T12:00:00Z'));
        utils = loadScript('utils.js', [
            'formatDate',
            'formatDateTime',
            'escapeHTML',
            'showAlert',
            'validateEmail',
            'validateTelefono',
            'isFutureDate',
            'localToISO',
        ]);

        expect(utils.isFutureDate('2026-07-08T12:00:01Z')).toBe(true);
        expect(utils.isFutureDate('2026-07-08T11:59:59Z')).toBe(false);
        expect(utils.isFutureDate('2026-07-08T12:00:00Z')).toBe(false);
    });

    test('localToISO devuelve una cadena ISO válida', () => {
        const resultado = utils.localToISO('2026-07-08T10:30');

        expect(resultado).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
    });
});