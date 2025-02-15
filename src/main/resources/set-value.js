(function(webelement, text) {

  function trigger(target, ...eventNames) {
    for (const i in eventNames) {
      try {
        const event = document.createEvent('HTMLEvents');
        event.initEvent(eventNames[i], true, true);
        target.dispatchEvent(event);
      }
      catch (staleElementException) {
        console.log('failed to trigger event', eventNames[i])
      }
    }
  }

  if (webelement.getAttribute('readonly') !== null) return 'Cannot change value of readonly element';
  if (webelement.getAttribute('disabled') !== null) return 'Cannot change value of disabled element';

  trigger(document.activeElement, 'blur');
  webelement.focus();
  const maxlength = webelement.getAttribute('maxlength') == null ? -1 : parseInt(webelement.getAttribute('maxlength'));
  webelement.value = maxlength === -1 ? text : text.length <= maxlength ? text : text.substring(0, maxlength);
  trigger(webelement, 'focus', 'keydown', 'keypress', 'input', 'keyup', 'change');

  return "";
})(arguments[0], arguments[1]);

