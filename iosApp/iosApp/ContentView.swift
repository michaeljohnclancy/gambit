import SwiftUI
import shared

struct ContentView: View {
    let mat = Mat()

	var body: some View {
        Text(String(mat.type()))
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
