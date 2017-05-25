package com.unicolour.joyspace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleValue<T>
{
	private T value;

	public SimpleValue()
    {
    }

    public SimpleValue(T value)
    { 
    	this.value = value; 
    }
    
    @JsonProperty("Value")
    public T getValue() {
		return value;
	}
    
    public void setValue(T value) {
		this.value = value;
	}
}