import { pagarConBanky, describirResultado } from './bankyCheckout.js';
import { firmarConHSMSignCR, describirResultadoFirma } from './hsmSignCheckout.js';

const $ = selector => document.querySelector(selector);
const $$ = selector => [...document.querySelectorAll(selector)];
let clienteActual = null;
let facturas = [];
let configPublica = {};
let lineasFactura = [];
let lineaEditando = null;

function showMessage(text, type = 'info') {
  const toast = $('#globalMessage');
  toast.textContent = text;
  toast.dataset.type = type;
  toast.classList.remove('hidden');
  clearTimeout(showMessage.timer);
  showMessage.timer = setTimeout(() => toast.classList.add('hidden'), 4500);
}

async function request(url, options = {}, responseType = 'json') {
  const token = localStorage.getItem('fe_token');
  const headers = {...(options.headers || {})};
  if (!(options.body instanceof FormData) && options.body != null) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(url, {...options, headers});

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      if (token && response.status === 401) logout(false);
    }
    const error = await response.json().catch(() => ({message: response.statusText}));
    throw new Error(error.message || `Error HTTP ${response.status}`);
  }
  if (response.status === 204) return null;
  if (responseType === 'blob') return response.blob();
  if (responseType === 'text') return response.text();
  return response.json();
}

function saveAuth(data) {
  localStorage.setItem('fe_token', data.accessToken);
  clienteActual = data.cliente;
  localStorage.setItem('fe_cliente', JSON.stringify(clienteActual));
  showApp();
}

function logout(message = true) {
  localStorage.removeItem('fe_token');
  localStorage.removeItem('fe_cliente');
  clienteActual = null;
  facturas = [];
  $('#appShell').classList.add('hidden');
  $('#authScreen').classList.remove('hidden');
  if (message) $('#authMessage').textContent = 'Sesión cerrada correctamente.';
}

function initials(name = 'FS') {
  return name.split(/\s+/).filter(Boolean).slice(0,2).map(x => x[0]).join('').toUpperCase();
}

function setView(id) {
  $$('.view').forEach(v => v.classList.toggle('active-view', v.id === id));
  $$('.nav-item').forEach(n => n.classList.toggle('active', n.dataset.view === id));
  const titles = {dashboardView:'Resumen',facturasView:'Facturas',nuevaFacturaView:'Nueva factura',integracionesView:'Integraciones',perfilView:'Mi perfil'};
  $('#pageTitle').textContent = titles[id] || 'FacturaSmart';
  if (id === 'facturasView') loadInvoices();
  if (id === 'perfilView') renderProfile();
}

async function loadPublicConfig() {
  try {
    configPublica = await request('/api/v1/config/public');
    $('#dgtdState').textContent = configPublica.integracionesModo || 'SIMULADO';
    if (configPublica.bankyConfigurado) {
      $('#bankyState').textContent = 'CONFIGURADO';
      $('#bankyState').className = 'integration-state ok';
    }
    if (configPublica.facturaBonitaConfigurada) {
      $('#facturaBonitaState').textContent = 'ACTIVA';
      $('#facturaBonitaState').className = 'integration-state ok';
    }
    if (configPublica.hsmConfigurado) {
      $('#hsmState').textContent = 'ACTIVO';
      $('#hsmState').className = 'integration-state ok';
    }
  } catch (_) {}
}

async function loadMe() {
  clienteActual = await request('/api/v1/clientes/me');
  localStorage.setItem('fe_cliente', JSON.stringify(clienteActual));
  const name = clienteActual.nombreRazonSocial || 'Cliente';
  $('#headerName').textContent = name;
  $('#headerEmail').textContent = clienteActual.correo || '';
  $('#avatar').textContent = initials(name);
  $('#profileAvatar').textContent = initials(name);
}

function statusClass(estado = '') {
  if (estado === 'ACEPTADA') return 'ok';
  if (estado === 'RECHAZADA') return 'bad';
  if (estado.includes('PENDIENTE')) return 'waiting';
  return 'neutral';
}

function paymentClass(estado = '') {
  return estado === 'PAGADA' ? 'ok' : 'waiting';
}

function money(value, currency='CRC') {
  return new Intl.NumberFormat('es-CR',{style:'currency',currency: currency || 'CRC'}).format(Number(value || 0));
}

function invoiceCard(f) {
  return `<article class="invoice-row">
    <div class="invoice-icon">FE</div>
    <div class="invoice-main"><strong>${escapeHtml(f.id)}</strong><span>${escapeHtml(f.receptor || 'Sin receptor')}</span><small>${escapeHtml(f.fecha || '')}</small></div>
    <div class="invoice-total"><strong>${money(f.totalComprobante, f.moneda)}</strong><span class="state ${statusClass(f.estado)}">${escapeHtml(f.estado || '—')}</span><span class="state ${paymentClass(f.estadoPago)}">${escapeHtml(f.estadoPago || 'PENDIENTE')}</span></div>
    <button class="row-action" data-invoice="${escapeHtml(f.id)}">Ver</button>
  </article>`;
}

async function loadInvoices() {
  try {
    facturas = await request('/api/v1/facturas');
    const html = facturas.length ? facturas.map(invoiceCard).join('') : '<div class="empty-state">Aún no hay facturas registradas.</div>';
    $('#invoiceTable').innerHTML = html;
    $('#recentInvoices').innerHTML = facturas.length ? facturas.slice(0,4).map(invoiceCard).join('') : '<div class="empty-state">Aún no hay facturas registradas.</div>';
    $('#statTotal').textContent = facturas.length;
    $('#statAceptadas').textContent = facturas.filter(f => f.estado === 'ACEPTADA').length;
    $('#statPendientes').textContent = facturas.filter(f => (f.estado || '').includes('PENDIENTE')).length;
    $('#statXml').textContent = facturas.filter(f => f.xmlValido).length;
    $$('[data-invoice]').forEach(b => b.onclick = () => openInvoice(b.dataset.invoice));
  } catch (e) { showMessage(e.message, 'error'); }
}

async function openInvoice(id) {
  try {
    const f = await request(`/api/v1/facturas/${encodeURIComponent(id)}`);
    let envios = [];
    try { envios = await request(`/api/v1/facturas/${encodeURIComponent(id)}/envios`); } catch (_) {}

    const pagoRealizado = f.estadoPago === 'PAGADA';
    const pagoDetalle = pagoRealizado ? `
      <div class="payment-summary">
        <span class="kicker">PAGO BANKYFINANZAS</span>
        <div class="detail-grid">
          <div><small>Estado</small><strong class="payment-ok">PAGADA ✓</strong></div>
          <div><small>Monto pagado</small><strong>${money(f.montoPagado || f.totalComprobante, f.moneda)}</strong></div>
          <div><small>Fecha de pago</small><strong>${escapeHtml(f.fechaPago || '—')}</strong></div>
          <div><small>Transacción</small><strong>${escapeHtml(f.codigoTransaccionBanky || '—')}</strong></div>
          <div><small>Tarjeta</small><strong>${escapeHtml(f.marcaTarjeta || 'Tarjeta')}${f.ultimosDigitosTarjeta ? ' ****' + escapeHtml(f.ultimosDigitosTarjeta) : ''}</strong></div>
          <div><small>Titular</small><strong>${escapeHtml(f.titularTarjeta || '—')}</strong></div>
        </div>
      </div>` : '';

    const botonPago = pagoRealizado
      ? '<button class="primary-button inline" disabled>Factura pagada ✓</button>'
      : '<button class="primary-button inline" id="payBanky">Pagar con Banky</button>';

    const botonFirma = f.firmaValida
      ? '<button class="primary-button inline" disabled>Firma HSM válida ✓</button>'
      : (f.xmlValido ? '<button class="primary-button inline" id="signHsm">Firmar con HSM</button>' : '');

    const firmaDetalle = f.firmaValida ? `
      <div class="payment-summary">
        <span class="kicker">FIRMA DIGITAL HSM SIGN CR</span>
        <div class="detail-grid">
          <div><small>Validación</small><strong class="payment-ok">VÁLIDA ✓</strong></div>
          <div><small>Firmante</small><strong>${escapeHtml(f.firmanteNombre || '—')}</strong></div>
          <div><small>Identificación</small><strong>${escapeHtml(f.firmanteIdentificacion || '—')}</strong></div>
          <div><small>Certificado</small><strong>${escapeHtml(f.estadoCertificado || '—')}</strong></div>
          <div><small>Algoritmo</small><strong>${escapeHtml(f.algoritmoFirma || '—')}</strong></div>
          <div><small>Fecha firma</small><strong>${escapeHtml(f.fechaFirmaHsm || '—')}</strong></div>
        </div>
      </div>` : '';

    $('#invoiceDialogBody').innerHTML = `
      <span class="kicker">DETALLE DE FACTURA</span><h2>${escapeHtml(f.id)}</h2>
      <div class="detail-grid"><div><small>Receptor</small><strong>${escapeHtml(f.receptor || '—')}</strong></div><div><small>Total</small><strong>${money(f.totalComprobante, f.moneda)}</strong></div><div><small>Estado factura</small><strong>${escapeHtml(f.estado)}</strong></div><div><small>Estado pago</small><strong class="state ${paymentClass(f.estadoPago)}">${escapeHtml(f.estadoPago || 'PENDIENTE')}</strong></div><div><small>XML</small><strong>${f.xmlValido ? 'Válido' : 'Pendiente'}</strong></div><div><small>Firma HSM</small><strong>${f.firmaValida ? 'Válida ✓' : 'Pendiente'}</strong></div></div>
      ${firmaDetalle}
      ${pagoDetalle}
      <div class="dialog-actions"><button class="secondary-button" id="viewXml">Ver XML</button><button class="secondary-button" id="downloadPdf">PDF local</button><button class="pretty-button" id="downloadPrettyPdf">Factura bonita PDF</button><button class="secondary-button" id="viewPrettyHtml">Vista bonita</button>${botonFirma}${botonPago}<button class="primary-button inline" id="sendDgtd">Enviar DGTD</button></div>
      <h4>Historial de envíos</h4>
      <div class="history-list">${envios.length ? envios.map(e => `<div><span class="state ${statusClass(e.estado)}">${escapeHtml(e.estado)}</span><strong>${escapeHtml(e.numeroAcuse || 'Sin acuse')}</strong><small>${escapeHtml(e.mensajeRespuesta || '')}</small></div>`).join('') : '<p class="muted">Sin envíos registrados.</p>'}</div>`;
    $('#invoiceDialog').showModal();
    $('#viewXml').onclick = () => openXml(id);
    $('#downloadPdf').onclick = () => downloadPdf(id);
    $('#downloadPrettyPdf').onclick = () => downloadPrettyPdf(id);
    $('#viewPrettyHtml').onclick = () => openPrettyHtml(id);
    if (!f.firmaValida && f.xmlValido) $('#signHsm').onclick = () => firmarFacturaConHsm(f);
    if (!pagoRealizado) $('#payBanky').onclick = () => pagarFacturaConBanky(f);
    $('#sendDgtd').onclick = async () => { try { const r=await request(`/api/v1/facturas/${encodeURIComponent(id)}/enviar`,{method:'POST'}); showMessage(`DGTD: ${r.estado}${r.numeroAcuse ? ' · '+r.numeroAcuse : ''}`,'success'); $('#invoiceDialog').close(); await loadInvoices(); } catch(e){showMessage(e.message,'error');} };
  } catch (e) { showMessage(e.message,'error'); }
}

async function firmarFacturaConHsm(factura) {
  try {
    await loadPublicConfig();
    if (!configPublica.hsmConfigurado) {
      throw new Error('HSM Sign CR todavía no está configurado.');
    }
    if (!clienteActual?.numeroIdentificacion) {
      throw new Error('Tu cuenta no tiene una identificación disponible para la firma.');
    }

    const xmlFactura = await request(
      `/api/v1/facturas/${encodeURIComponent(factura.id)}/xml`,
      {},
      'text'
    );

    showMessage('Abriendo HSM Sign CR. El PIN se ingresa únicamente en la ventana de firma.', 'info');

    const resultado = await firmarConHSMSignCR({
      identificacion: clienteActual.numeroIdentificacion,
      xmlFactura
    }, configPublica);

    if (resultado?.status !== 'completed') {
      showMessage(
        describirResultadoFirma(resultado),
        resultado?.status === 'rejected' ? 'error' : 'info'
      );
      return;
    }

    const actualizada = await request(
      `/api/v1/facturas/${encodeURIComponent(factura.id)}/firma-hsm`,
      {
        method: 'POST',
        body: JSON.stringify({
          xmlFirmado: resultado.xmlFirmado,
          hashDocumento: resultado.hashDocumento || null,
          serialCertificado: resultado.serialCertificado || null
        })
      }
    );

    showMessage(
      `Firma validada por HSM Sign CR. Factura ${actualizada.id} marcada como FIRMADA.`,
      'success'
    );
    $('#invoiceDialog').close();
    await loadInvoices();
    await openInvoice(actualizada.id);
  } catch (e) {
    showMessage(e.message, 'error');
  }
}

async function pagarFacturaConBanky(factura) {
  try {
    await loadPublicConfig();
    if (!configPublica.bankyConfigurado) throw new Error('BankyFinanzas todavía no está configurado.');

    showMessage(`Abriendo BankyFinanzas para pagar ${money(factura.totalComprobante, factura.moneda)}...`, 'info');
    const resultado = await pagarConBanky({
      orderId: factura.id,
      amount: factura.totalComprobante,
      description: `Pago factura ${factura.id} - FacturaSmart`,
      currency: factura.moneda || configPublica.bankyCurrency || 'CRC'
    }, configPublica);

    if (resultado?.status === 'completed') {
      const actualizada = await request(`/api/v1/facturas/${encodeURIComponent(factura.id)}/pago-banky`, {
        method: 'POST',
        body: JSON.stringify({
          status: resultado.status,
          transactionCode: resultado.transactionCode,
          cardBrand: resultado.cardBrand || null,
          cardLastFourDigits: resultado.cardLastFourDigits || null,
          cardholderName: resultado.cardholderName || null
        })
      });
      showMessage(`Pago aprobado. Factura ${actualizada.id} marcada como PAGADA.`, 'success');
      $('#invoiceDialog').close();
      await loadInvoices();
      await openInvoice(actualizada.id);
      return;
    }

    showMessage(describirResultado(resultado), resultado?.status === 'rejected' ? 'error' : 'info');
  } catch(e) {
    showMessage(e.message, 'error');
  }
}

async function openXml(id) {
  try {
    const xml = await request(`/api/v1/facturas/${encodeURIComponent(id)}/xml`, {}, 'text');
    const w = window.open('', '_blank');
    if (!w) throw new Error('El navegador bloqueó la ventana del XML.');
    w.document.write(`<pre style="white-space:pre-wrap;font:14px monospace;padding:24px">${escapeHtml(xml)}</pre>`);
    w.document.close();
  } catch(e){showMessage(e.message,'error');}
}

async function downloadPdf(id) {
  try {
    const blob = await request(`/api/v1/facturas/${encodeURIComponent(id)}/pdf`, {}, 'blob');
    const url = URL.createObjectURL(blob); const a=document.createElement('a'); a.href=url; a.download=`${id}.pdf`; a.click(); URL.revokeObjectURL(url);
  } catch(e){showMessage(e.message,'error');}
}

async function downloadPrettyPdf(id) {
  try {
    showMessage('Generando factura bonita con el servicio compartido...', 'info');
    const blob = await request(`/api/v1/facturas/${encodeURIComponent(id)}/pdf-bonita`, {}, 'blob');
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${id}-factura-bonita.pdf`;
    a.click();
    URL.revokeObjectURL(url);
    showMessage('Factura bonita generada correctamente.', 'success');
  } catch(e){showMessage(e.message,'error');}
}

async function openPrettyHtml(id) {
  const w = window.open('', '_blank');
  if (!w) {
    showMessage('El navegador bloqueó la ventana de la factura bonita.', 'error');
    return;
  }
  w.document.write('<p style="font:14px sans-serif;padding:24px">Generando factura bonita...</p>');
  try {
    showMessage('Preparando vista bonita...', 'info');
    const html = await request(`/api/v1/facturas/${encodeURIComponent(id)}/html-bonita`, {}, 'text');
    w.document.open();
    w.document.write(html);
    w.document.close();
  } catch(e){
    w.close();
    showMessage(e.message,'error');
  }
}

function renderProfile() {
  if (!clienteActual) return;
  $('#profileInfo').innerHTML = `<h3>${escapeHtml(clienteActual.nombreRazonSocial)}</h3><p>${escapeHtml(clienteActual.correo)}</p><div class="detail-grid"><div><small>Identificación</small><strong>${escapeHtml(clienteActual.tipoIdentificacion)} · ${escapeHtml(clienteActual.numeroIdentificacion)}</strong></div><div><small>Teléfono</small><strong>${escapeHtml(clienteActual.telefono || '—')}</strong></div><div><small>Estado</small><strong>${escapeHtml(clienteActual.estado)}</strong></div><div><small>Rol</small><strong>${escapeHtml(clienteActual.rol)}</strong></div></div>`;
}

async function showApp() {
  $('#authScreen').classList.add('hidden'); $('#appShell').classList.remove('hidden');
  try { await Promise.all([loadMe(), loadPublicConfig()]); await loadInvoices(); setView('dashboardView'); }
  catch(e){ showMessage(e.message,'error'); }
}

function escapeHtml(v='') { return String(v).replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c])); }

$$('[data-auth-tab]').forEach(btn => btn.onclick = () => {
  $$('[data-auth-tab]').forEach(b=>b.classList.toggle('active',b===btn));
  $$('[data-auth-panel]').forEach(p=>p.classList.toggle('hidden',p.dataset.authPanel!==btn.dataset.authTab));
  $('#authMessage').textContent='';
});

$('#loginForm').onsubmit = async e => { e.preventDefault(); const f=Object.fromEntries(new FormData(e.target)); try{saveAuth(await request('/api/v1/auth/login',{method:'POST',body:JSON.stringify(f)}));}catch(x){$('#authMessage').textContent=x.message;} };
$('#registerForm').onsubmit = async e => { e.preventDefault(); const f=Object.fromEntries(new FormData(e.target)); try{saveAuth(await request('/api/v1/auth/registro',{method:'POST',body:JSON.stringify(f)}));}catch(x){$('#authMessage').textContent=x.message;} };
$('#logout').onclick=()=>logout();
$$('.nav-item').forEach(n=>n.onclick=()=>setView(n.dataset.view));
$$('[data-go]').forEach(n=>n.onclick=()=>setView(n.dataset.go));
$('#refreshInvoices').onclick=loadInvoices;
$('#closeDialog').onclick=()=>$('#invoiceDialog').close();

function roundMoney(value) {
  return Math.round((Number(value) + Number.EPSILON) * 100) / 100;
}

function currencyFromInvoiceForm() {
  return $('#invoiceForm [name="moneda"]')?.value || 'CRC';
}

function calcularLinea(linea) {
  const subtotal = roundMoney(linea.cantidad * linea.precioUnitario);
  const impuesto = roundMoney(subtotal * (linea.tarifa / 100));
  return {...linea, subtotal, impuesto, montoTotalLinea: roundMoney(subtotal + impuesto)};
}

function resumenLineas() {
  return lineasFactura.reduce((acc, linea) => {
    const calc = calcularLinea(linea);
    if (calc.tarifa > 0) acc.totalGravado += calc.subtotal;
    else acc.totalExento += calc.subtotal;
    acc.totalImpuesto += calc.impuesto;
    acc.totalComprobante += calc.montoTotalLinea;
    return acc;
  }, {totalGravado:0,totalExento:0,totalImpuesto:0,totalComprobante:0});
}

function renderLineasFactura() {
  const contenedor = $('#invoiceLines');
  const moneda = currencyFromInvoiceForm();
  $('#lineCountBadge').textContent = `${lineasFactura.length} ${lineasFactura.length === 1 ? 'línea' : 'líneas'}`;

  if (!lineasFactura.length) {
    contenedor.className = 'invoice-lines-empty';
    contenedor.innerHTML = 'Aún no has agregado productos o servicios.';
  } else {
    contenedor.className = 'invoice-lines';
    contenedor.innerHTML = `
      <div class="invoice-lines-header"><span>Descripción</span><span>Cant.</span><span>Precio</span><span>IVA</span><span>Total</span><span>Acciones</span></div>
      ${lineasFactura.map((linea, index) => {
        const calc = calcularLinea(linea);
        return `<div class="invoice-line-row">
          <div><strong>${escapeHtml(linea.detalle)}</strong><small>Línea ${index + 1}</small></div>
          <span>${linea.cantidad}</span>
          <span>${money(linea.precioUnitario, moneda)}</span>
          <span>${linea.tarifa}%</span>
          <strong>${money(calc.montoTotalLinea, moneda)}</strong>
          <div class="line-actions"><button type="button" class="line-action edit" data-edit-line="${index}">Editar</button><button type="button" class="line-action remove" data-remove-line="${index}">Eliminar</button></div>
        </div>`;
      }).join('')}`;
  }

  const resumen = resumenLineas();
  $('#previewGravado').textContent = money(roundMoney(resumen.totalGravado), moneda);
  $('#previewExento').textContent = money(roundMoney(resumen.totalExento), moneda);
  $('#previewImpuesto').textContent = money(roundMoney(resumen.totalImpuesto), moneda);
  $('#previewTotal').textContent = money(roundMoney(resumen.totalComprobante), moneda);

  $$('[data-remove-line]').forEach(btn => btn.onclick = () => {
    const index = Number(btn.dataset.removeLine);
    lineasFactura.splice(index, 1);
    if (lineaEditando === index) cancelarEdicionLinea();
    else if (lineaEditando != null && lineaEditando > index) lineaEditando -= 1;
    renderLineasFactura();
  });

  $$('[data-edit-line]').forEach(btn => btn.onclick = () => editarLinea(Number(btn.dataset.editLine)));
}

function limpiarEditorLinea() {
  $('#itemDetalle').value = '';
  $('#itemCantidad').value = '1';
  $('#itemPrecio').value = '';
  $('#itemTarifa').value = '13';
}

function cancelarEdicionLinea() {
  lineaEditando = null;
  limpiarEditorLinea();
  $('#addInvoiceLine').textContent = '+ Agregar línea';
  $('#cancelLineEdit').classList.add('hidden');
}

function editarLinea(index) {
  const linea = lineasFactura[index];
  if (!linea) return;
  lineaEditando = index;
  $('#itemDetalle').value = linea.detalle;
  $('#itemCantidad').value = linea.cantidad;
  $('#itemPrecio').value = linea.precioUnitario;
  $('#itemTarifa').value = linea.tarifa;
  $('#addInvoiceLine').textContent = 'Actualizar línea';
  $('#cancelLineEdit').classList.remove('hidden');
  $('#itemDetalle').focus();
}

function leerEditorLinea() {
  const detalle = $('#itemDetalle').value.trim();
  const cantidad = Number($('#itemCantidad').value);
  const precioUnitario = Number($('#itemPrecio').value);
  const tarifa = Number($('#itemTarifa').value);

  if (!detalle) throw new Error('Escribe la descripción del producto o servicio.');
  if (!Number.isInteger(cantidad) || cantidad <= 0) throw new Error('La cantidad debe ser un número entero mayor que 0.');
  if (!Number.isFinite(precioUnitario) || precioUnitario <= 0) throw new Error('El precio unitario debe ser mayor que 0.');
  if (!Number.isFinite(tarifa) || tarifa < 0) throw new Error('El IVA no puede ser negativo.');

  return {detalle, cantidad, precioUnitario:roundMoney(precioUnitario), tarifa:roundMoney(tarifa)};
}

$('#addInvoiceLine').onclick = () => {
  try {
    const linea = leerEditorLinea();
    if (lineaEditando == null) lineasFactura.push(linea);
    else lineasFactura[lineaEditando] = linea;
    cancelarEdicionLinea();
    renderLineasFactura();
    showMessage('Línea agregada a la factura.', 'success');
  } catch (e) {
    showMessage(e.message, 'error');
  }
};

$('#cancelLineEdit').onclick = cancelarEdicionLinea;
$('#invoiceForm [name="moneda"]').onchange = renderLineasFactura;

$('#invoiceForm').onsubmit = async e => {
  e.preventDefault();
  if (!clienteActual) return;
  if (!lineasFactura.length) {
    showMessage('Agrega al menos un producto o servicio antes de generar la factura.', 'error');
    return;
  }

  const f = Object.fromEntries(new FormData(e.target));
  const items = lineasFactura.map((linea, index) => {
    const calc = calcularLinea(linea);
    return {
      numeroLinea:index + 1,
      detalle:linea.detalle,
      cantidad:linea.cantidad,
      precioUnitario:linea.precioUnitario,
      descuento:0,
      impuesto:{tarifa:linea.tarifa},
      subtotal:calc.subtotal,
      montoTotalLinea:calc.montoTotalLinea
    };
  });
  const resumen = resumenLineas();
  const body={
    id:f.id,
    fecha:f.fecha,
    moneda:f.moneda,
    condicionVenta:f.condicionVenta,
    medioPago:f.medioPago,
    tipoDocumento:'FACTURA_ELECTRONICA',
    emisor:{nombre:clienteActual.nombreRazonSocial,identificacion:{tipo:clienteActual.tipoIdentificacion,numero:clienteActual.numeroIdentificacion},correo:clienteActual.correo},
    receptor:{nombre:f.receptorNombre,identificacion:{tipo:f.receptorTipo,numero:f.receptorNumero},correo:f.receptorCorreo||null},
    items,
    totales:{
      totalGravado:roundMoney(resumen.totalGravado),
      totalExento:roundMoney(resumen.totalExento),
      totalDescuentos:0,
      totalImpuesto:roundMoney(resumen.totalImpuesto),
      totalComprobante:roundMoney(resumen.totalComprobante)
    }
  };

  try {
    const r=await request('/api/v1/facturas/procesar',{method:'POST',body:JSON.stringify(body)});
    $('#invoiceFormResult').textContent=`Factura ${r.id} creada correctamente.`;
    showMessage(`Factura ${r.id} creada con ${items.length} ${items.length === 1 ? 'línea' : 'líneas'} y XML validado.`,'success');
    lineasFactura = [];
    lineaEditando = null;
    renderLineasFactura();
    e.target.reset();
    $('#invoiceForm [name="moneda"]').value = 'CRC';
    $('#invoiceForm [name="condicionVenta"]').value = 'CONTADO';
    $('#invoiceForm [name="medioPago"]').value = 'SINPE';
    limpiarEditorLinea();
    await loadInvoices();
  }
  catch(err){ $('#invoiceFormResult').textContent=err.message; showMessage(err.message,'error'); }
};

renderLineasFactura();

$('#bankyDemo').onclick = async () => {
  try {
    await loadPublicConfig();
    const resultado = await pagarConBanky({orderId:`FACTURA-SERVICE-${Date.now()}`,amount:1000,description:'Prueba servicio FacturaSmart'}, configPublica);
    showMessage(describirResultado(resultado), resultado?.status==='completed'?'success':'info');
  } catch(e){showMessage(e.message,'error');}
};

if (localStorage.getItem('fe_token')) showApp(); else loadPublicConfig();
