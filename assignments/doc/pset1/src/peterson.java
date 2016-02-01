class PetersonsAlgoritihm implements Lock {
	boolean wantCS [] = {false, false};

	public void requestCS0() {
		wantCS[0] = true;
		turn0 = 1;
		while(wantCS[1] && (turn1 == 1)) ;
	}

	public void requestCS1() {
		wantCS[1] = true;
		turn1 = 1;
		while (wantCS[0] && (turn0 == 1) && (turn1 == 1)) ;
	}

	public void releaseCS(int i) {
		wantCS[i] = false;
		if (i == 0) {
			turn0 = 0;
		} else {
			turn1 = 0;
		}
	}
}
