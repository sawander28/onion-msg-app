import SwiftUI

struct ContentView: View {
    var body: some View {
        let cacheDir = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!;

        let result = start_arti_proxy("ifconfig.me", cacheDir.path)!;
        let message = String(cString: result)
        my_rust_function_free_result(UnsafeMutablePointer(mutating:  result));

        let text = Text(message)
            .padding()

        return text
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
