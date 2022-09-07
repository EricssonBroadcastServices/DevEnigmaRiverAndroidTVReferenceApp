package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new com.redbeemedia.enigma.referenceapp.DataBinderMapperImpl());
  }
}
