package brightspark.structuralrelocation.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

public class RedrawableTesselator
{
	private final BufferBuilder buffer;

	public RedrawableTesselator(int bufferSize, int glMode, VertexFormat format, Consumer<BufferBuilder> bufferInit)
	{
		buffer = new BufferBuilder(bufferSize);
		buffer.begin(glMode, format);
		bufferInit.accept(buffer);
		buffer.finishDrawing();
	}

	//Copied from WorldVertexBufferUploader#draw and edited slightly
	public void draw()
	{
		if (buffer.getVertexCount() > 0)
		{
			VertexFormat vertexformat = buffer.getVertexFormat();
			int i = vertexformat.getSize();
			ByteBuffer bytebuffer = buffer.getByteBuffer();
			List<VertexFormatElement> list = vertexformat.getElements();

			for (int j = 0; j < list.size(); ++j)
			{
				VertexFormatElement vertexformatelement = list.get(j);
				bytebuffer.position(vertexformat.getOffset(j));
				vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
			}

			GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
			int i1 = 0;

			for (int j1 = list.size(); i1 < j1; ++i1)
			{
				VertexFormatElement vertexformatelement1 = list.get(i1);
				vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
			}
		}
	}
}
