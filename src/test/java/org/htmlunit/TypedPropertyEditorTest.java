package org.htmlunit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests the {@link TypedPropertyEditor} class.
 */
public class TypedPropertyEditorTest {

  @Test
  public void setValue() {
    TypedPropertyEditor editor = new TypedPropertyEditor();
    assertThat(editor.isEditable(), is(false));
    assertThat(editor.getValue(), is(nullValue()));

    editor.setValue(1234L);
    assertThat(editor.isEditable(), is(true));
    assertThat((Integer) editor.getValue(), is(1234));

    editor.setValue("1337");
    assertThat(editor.isEditable(), is(true));
    assertThat((Integer) editor.getValue(), is(1337));

    editor.setValue("true");
    assertThat(editor.isEditable(), is(true));
    assertThat((Boolean) editor.getValue(), is(true));

    editor.setValue("false");
    assertThat(editor.isEditable(), is(true));
    assertThat((Boolean) editor.getValue(), is(false));

    editor.setValue("Cannot edit");
    assertThat(editor.isEditable(), is(false));
    assertThat((String) editor.getValue(), is("Cannot edit"));

    editor.setAsText("1234");
    assertThat(editor.isEditable(), is(true));
    assertThat(editor.getAsText(), is("1234"));
    assertThat((Integer) editor.getValue(), is(1234));
  }
}
