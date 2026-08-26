const CANAL_CHECKOUT = 'bankyfinanzas:checkout';
const ANCHO_VENTANA = 560;
const ALTO_VENTANA = 760;
const INTERVALO_VIGILANCIA_MS = 500;

const MENSAJES_RECHAZO = {
  INSUFFICIENT_FUNDS: 'La tarjeta no tiene fondos suficientes.',
  CARD_EXPIRED: 'La tarjeta se encuentra vencida.',
  CARD_DECLINED: 'El cobro supera el límite permitido por transacción.',
  INVALID_CARD: 'Los datos de la tarjeta no son válidos.',
  NETWORK_ERROR: 'No se pudo contactar al banco emisor.'
};

export function pagarConBanky(datos, configuracion) {
  return new Promise((resolve, reject) => {
    const bankyUrl = (configuracion?.bankyUrl || '').replace(/\/$/, '');
    const merchantId = configuracion?.bankyMerchantId || '';
    const currency = datos.currency || configuracion?.bankyCurrency || 'CRC';

    if (!bankyUrl || !merchantId) {
      reject(new Error('BankyFinanzas todavía no está configurado. Defina BANKY_MERCHANT_ID.'));
      return;
    }

    const parametros = new URLSearchParams({
      merchantId,
      orderId: datos.orderId,
      amount: String(datos.amount),
      description: datos.description,
      currency,
      origin: window.location.origin
    });

    const ventana = window.open(`${bankyUrl}/checkout?${parametros.toString()}`, 'bankyfinanzas-checkout', `width=${ANCHO_VENTANA},height=${ALTO_VENTANA}`);
    if (!ventana) {
      reject(new Error('El navegador bloqueó la ventana de BankyFinanzas. Habilite ventanas emergentes e intente de nuevo.'));
      return;
    }

    let finalizado = false;
    const limpiar = () => {
      window.removeEventListener('message', manejarMensaje);
      clearInterval(intervaloVigilancia);
    };
    const manejarMensaje = evento => {
      if (evento.origin !== bankyUrl) return;
      const payload = evento.data || {};
      if (payload.channel !== CANAL_CHECKOUT || !payload.result) return;
      finalizado = true;
      limpiar();
      resolve(payload.result);
    };
    const intervaloVigilancia = setInterval(() => {
      if (ventana.closed && !finalizado) {
        finalizado = true;
        limpiar();
        resolve({status: 'cancelled'});
      }
    }, INTERVALO_VIGILANCIA_MS);
    window.addEventListener('message', manejarMensaje);
  });
}

export function describirResultado(resultado) {
  if (resultado?.status === 'completed') return 'El pago se procesó correctamente.';
  if (resultado?.status === 'cancelled') return 'El pago fue cancelado antes de finalizar.';
  if (resultado?.status === 'rejected') return MENSAJES_RECHAZO[resultado.rejectionCode] || 'El pago fue rechazado por el banco emisor.';
  return 'No fue posible determinar el resultado del pago.';
}
