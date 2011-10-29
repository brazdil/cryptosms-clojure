package uk.ac.cam.db538.cryptosms;

public class IdentificationNumber {
	public enum IdType {
		PHONE_NUMBER,
		SIM_SERIAL_NUMBER
	}
	
	private IdType mType;
	private String mId;
	
	public IdentificationNumber(IdType type, String id) {
		mType = type;
		mId = id;
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat) return true;
		if (!(aThat instanceof IdentificationNumber)) return false;
		
		IdentificationNumber that = (IdentificationNumber) aThat; 
		return (this.mType.equals(that.mType)) &&
		       (this.mId.equals(that.mId));
	}
	
	@Override
	public int hashCode() {
		return mId.hashCode() + mType.hashCode();
	}
}
