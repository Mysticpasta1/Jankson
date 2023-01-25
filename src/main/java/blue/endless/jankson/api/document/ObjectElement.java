/*
 * MIT License
 *
 * Copyright (c) 2018-2022 Falkreon (Isaac Ellingson)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package blue.endless.jankson.api.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import blue.endless.jankson.api.io.StructuredDataWriter;

public class ObjectElement implements ValueElement {
	protected boolean isDefault = false;
	protected List<NonValueElement> preamble = new ArrayList<>();
	protected List<KeyValuePairElement> entries = new ArrayList<>();
	protected List<NonValueElement> footer = new ArrayList<>();
	protected List<NonValueElement> epilogue = new ArrayList<>();
	
	@Override
	public List<NonValueElement> getPreamble() {
		return preamble;
	}
	
	/**
	 * Gets NonValueElements following the last key-value pair in this ObjectElement
	 */
	public List<NonValueElement> getFooter() {
		return footer;
	}
	
	@Override
	public List<NonValueElement> getEpilogue() {
		return epilogue;
	}
	
	@Nullable
	public ValueElement get(String key) {
		for(DocumentElement entry : entries) {
			if (entry instanceof KeyValuePairElement) {
				if (((KeyValuePairElement) entry).getKey().equals(key)) {
					return ((KeyValuePairElement) entry).getValue();
				}
			}
		}
		
		return null;
	}
	
	public DocumentElement put(String key, ValueElement value) {
		//Validate
		if (
				value instanceof KeyValuePairElement ||
				value instanceof CommentElement) throw new IllegalArgumentException();
		
		for(DocumentElement entry : entries) {
			if (entry instanceof KeyValuePairElement) {
				KeyValuePairElement pair = (KeyValuePairElement) entry;
				
				if (pair.getKey().equals(key)) {
					return pair.setValue(value);
				}
			}
		}
		
		//No matching KeyValueDocumentEntry. Add one at the end of the object's sub-document
		entries.add(new KeyValuePairElement(key, value));
		return null;
	}
	
	@Override
	public ValueElement stripFormatting() {
		preamble.clear();
		footer.clear();
		epilogue.clear();
		
		return this;
	}
	
	@Override
	public ObjectElement stripAllFormatting() {
		preamble.clear();
		
		for(KeyValuePairElement elem : entries) {
			elem.stripAllFormatting();
		}
		
		footer.clear();
		epilogue.clear();
		
		return this;
	}
	
	public ObjectElement clone() {
		ObjectElement result = new ObjectElement();
		for(NonValueElement elem : preamble) {
			result.preamble.add(elem.clone());
		}
		
		for(KeyValuePairElement elem : entries) {
			result.entries.add(elem.clone());
		}
		
		for(NonValueElement elem : footer) {
			result.footer.add(elem.clone());
		}
		
		for(NonValueElement elem : epilogue) {
			result.epilogue.add(elem.clone());
		}
		
		result.isDefault = isDefault;
		
		return result;
	}
	
	@Override
	public boolean isDefault() {
		return isDefault;
	}
	
	@Override
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public void write(StructuredDataWriter writer) throws IOException {
		for(NonValueElement elem : preamble) elem.write(writer);
		
		writer.writeObjectStart();
		
		for(int i=0; i<entries.size(); i++) {
			KeyValuePairElement elem = entries.get(i);
			writer.writeKey(elem.getKey());
			writer.startValue();
			elem.write(writer);
			
			if (i<entries.size()-1) writer.nextValue();
		}
		
		for(NonValueElement elem : footer) elem.write(writer);
		
		writer.writeObjectEnd();
		
		for(NonValueElement elem : epilogue) elem.write(writer);
	}
}
