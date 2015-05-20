package thegarlic.forum.service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

public class ModelMapperBuilder {
    
    private ModelMapper mapper;

    private ModelMapperBuilder() {
        this.mapper = new ModelMapper();
    }
    
    public ModelMapperBuilder but() {
        
        try {
            return (ModelMapperBuilder) this.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    
    public static ModelMapperBuilder modelMapper() {
        return new ModelMapperBuilder();
    }
    
    public ModelMapperBuilder withDateTimeToUTCString() {
        
        mapper.addConverter(new Converter<DateTime, String>() {
            @Override
            public String convert(MappingContext<DateTime, String> context) {
                return context.getSource() == null ? "" : context.getSource().withZone(DateTimeZone.UTC).toString();
            }
        });
        
        return this;
    }
    
    public ModelMapperBuilder withMapping(PropertyMap<?, ?> map) {
        
        mapper.addMappings(map);
        return this;
    }
    
    public ModelMapper build() {
        return mapper;
    }
}
