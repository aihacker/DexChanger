package huluwa.dexparser.format;

import java.util.ArrayList;
import java.util.List;

import huluwa.dexparser.Exception.CursorMoveException;
import huluwa.dexparser.Exception.NonStandardLeb128Exception;
import huluwa.dexparser.Exception.QueryNextDataException;
import huluwa.dexparser.base.Item;
import huluwa.dexparser.type.Leb128;

public class Code_Item extends Item {
	private static String itemName = "Code_Item";
	public short registers_size;
	public short ins_size;
	public short outs_size;
	public short tries_size;
	public int debug_info_off;
	public int insns_size;
	public byte insns[];
	public byte try_items[];
	public byte handle[];
	List<insns_item> insns_items;

	public Code_Item(byte data[], int off) {
		super(data, off);
	}

	@Override
	public int getLength() {
		int len = 16 + insns.length;
		if(tries_size != 0) {
			len += try_items.length + handle.length;
		}
		return len;
	}

	@Override
	public String getName() {
		return itemName;
	}

	@Override
	public void parseData() throws QueryNextDataException {
		insns_items = new ArrayList<insns_item>();
		try {
			this.registers_size = this.cursor.nextShort();
			this.ins_size = this.cursor.nextShort();
			this.outs_size = this.cursor.nextShort();
			this.tries_size = this.cursor.nextShort();
			this.debug_info_off = this.cursor.nextInt();
			this.insns_size = this.cursor.nextInt();
			this.insns = this.cursor.nextData(insns_size * 2);
			this.cursor.aboveMove(insns_size * 2);
			int i = 0;
			while (i < insns.length) {
				insns_item item = new insns_item(this.cursor.getBytes(), this.cursor.getPos());
				this.insns_items.add(item);
				this.cursor.belowMove(item.getLength());
				i += item.getLength();
			}
			if(tries_size != 0) {
				int try_len = tries_size * 8;
				if((insns_size & 1 )== 1) {
					try_len += 2;
				}
				try_items = this.cursor.nextData(try_len);
				Leb128 handle_size = this.cursor.nextLeb128();
				int handle_len = handle_size.getLength() + handle_size.toInt() * 5;
				this.cursor.aboveMove(handle_size.getLength());
				this.handle = this.cursor.nextData(handle_len);
			}
		} catch (CursorMoveException e) {
			System.out.println("[*E]" + getName() + ":" + e.getMessage());
		} catch (NonStandardLeb128Exception e) {
			System.out.println("[*E]" + getName() + ":" + e.getMessage());
		}
	}
}
