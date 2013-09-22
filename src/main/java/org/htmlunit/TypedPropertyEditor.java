package org.htmlunit;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/** Editor to edit native types. The {@link #isEditable()} method
 * indicates whether there's a valid editor or not after the value is set.
 * It will not throw an exception if the value cannot be edited, but return
 * the original value.
 *
 * <p>
 * It assumes each <em>type</em> has a <code>valueOf(String)</code> method.
 * Numbers are always treated as Integer. It doesn't support floating point
 * numbers.
 * </p>
 */
public class TypedPropertyEditor extends PropertyEditorSupport {

  /** Property editors to edit default types. */
  private static final List<TypeEditor> types = new ArrayList<TypeEditor>();

  /** Indicates whether a type editor can edit the current value. */
  private boolean editable;

  /** Default constructor.
   */
  public TypedPropertyEditor() {
    types.add(new BooleanEditor());
    types.add(new NumberEditor());
  }

  /** Sets the value only if one of registered type editors can edit the value.
   * @param value Value to edit. Cannot be null.
   */
  @Override
  public void setValue(final Object value) {
    for (TypeEditor editor : types) {
      if (editor.canEdit(value)) {
        editor.setValue(value);
        super.setValue(editor.getValue());
        editable = true;
        return;
      }
    }
    super.setValue(value);
    editable = false;
  }

  /** Indicates whether a type editor can edit the current value.
   *
   * @return Returns true if a value is already set and there's an editor to
   *    edit the current value, otherwise it returns false.
   */
  public boolean isEditable() {
    return editable;
  }

  /** Property editor to edit multiple typed values. Types must support a
   * <code>valueOf(String)</code> operation.
   */
  private abstract static class TypeEditor extends PropertyEditorSupport {

    /** Type to edit, it's never null. */
    private final Class<?> type;

    /** Creates a type editor and sets the editing type.
     *
     * NOTE: keep it abstract, property editors MUST support a default
     *    constructor.
     * @param theType Type to edit. Cannot be null.
     */
    public TypeEditor(final Class<?> theType) {
      Validate.notNull(theType, "The type cannot be null.");
      type = theType;
    }

    /** Indicates whether this editor can edit the specified value or not.
     * @param value Value to edit. It's never null.
     * @return Returns true if this editor can edit the value, false otherwise.
     */
    public abstract boolean canEdit(final Object value);

    /** {@inheritDoc}
     */
    @Override
    public Object getValue() {
      Object value = super.getValue();
      try {
        return type.getMethod("valueOf", new Class[] { String.class })
            .invoke(null, new Object[] { value.toString() });
      } catch (Exception cause) {
        throw new IllegalArgumentException("Cannot convert value.", cause);
      }
    }
  }

  /** Edits a boolean value. */
  private static class BooleanEditor extends TypeEditor {
    /** Default constructor. */
    public BooleanEditor() {
      super(Boolean.class);
    }

    /** {@inheritDoc}
     */
    @Override
    public boolean canEdit(final Object value) {
      return "true".equals(value) || "false".equals(value);
    }
  }

  /** Edits a scalar value. */
  private static class NumberEditor extends TypeEditor {
    /** Default constructor. */
    public NumberEditor() {
      super(Integer.class);
    }

    /** {@inheritDoc}
     */
    @Override
    public boolean canEdit(final Object value) {
      return StringUtils.isNumeric(String.valueOf(value));
    }
  }
}
