package org.htmlunit.javascript;

import com.gargoylesoftware.htmlunit.javascript.host.Event;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

/** Represents a JavaScript event handler. It can be used to pass around as
 * event listener.
 */
public abstract class EventHandler extends ScriptableObject
    implements Function {

  /** Default id for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   *  This method is called whenever an event occurs of the type for which
   * the <code> EventListener</code> interface was registered.
   * @param event  The <code>Event</code> contains contextual information
   *   about the event. It also contains the <code>stopPropagation</code>
   *   and <code>preventDefault</code> methods which are used in
   *   determining the event's flow and default action.
   */
  public abstract void handleEvent(final Event event);

  /** Delegates the JavaScript function call to the internal callback.
   *
   * <p>{@inheritDoc}</p>
   */
  public Object call(final Context cx, final Scriptable scope,
      final Scriptable thisObj, final Object[] args) {
    if (args.length == 1
        && com.gargoylesoftware.htmlunit.javascript.host
          .Event.class.isInstance(args[0])) {
      handleEvent((Event) args[0]);
    }
    return null;
  }

  /** {@inheritDoc}
   */
  public Scriptable construct(final Context cx, final Scriptable scope,
      final Object[] args) {
    throw new UnsupportedOperationException(
        "Cannot instantiate event handlers.");
  }

  /** {@inheritDoc}
   */
  @Override
  public String getClassName() {
    return getClass().getName();
  }
}
